package jrkim.rcash.kml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import jrkim.rcash.data.WhereCash;
import jrkim.rcash.data.WhereCashFolder;

public class RCashKmlParser {

    private final static String TAG = "RCash_Kml";

    private final XmlPullParser mParser;
    public String name = null;
    public String description = null;

    private final static int EVENT_TYPE_TAG_OPEN = 2;
    private final static int EVENT_TYPE_TAG_CLOSE = 3;
    private final static int EVENT_TYPE_TAG_TEXT = 4;

    private final static int DEPTH_ZERO = 0;
    private final static int DEPTH_KML = 1;
    private final static int DEPTH_DOCUMENT = 2;
    private final static int DEPTH_FOLDER = 3;

    public ArrayList<WhereCashFolder> arrFolders = new ArrayList<>();

    public RCashKmlParser(XmlPullParser parser) {
        this.mParser = parser;
    }

    public void parseKml() throws XmlPullParserException, IOException {
        /**
         * XmlParser를 전체 순회
         */
        int curDepth = 0;
        WhereCashFolder curFolder = null;
        for(int eventType = mParser.getEventType(); eventType != 1; eventType = mParser.next()) {
            if(eventType == EVENT_TYPE_TAG_OPEN) {

                if(mParser.getName().matches("Style|StyleMap|styleUrl")) {
                    skip();
                }

                if(curDepth == DEPTH_ZERO && mParser.getName().equals("kml")) {
                    curDepth = DEPTH_KML;
                }

                if(curDepth == DEPTH_KML && mParser.getName().equals("Document")) {
                    curDepth = DEPTH_DOCUMENT;
                    findNameAndDescriptionOfDocument();
                }

                if(curDepth == DEPTH_DOCUMENT && mParser.getName().equals("Folder")) {
                    curDepth = DEPTH_FOLDER;

                    if(curFolder != null) {
                        arrFolders.add(curFolder);
                    }

                    curFolder = new WhereCashFolder();
                    curFolder.folderName = findNextName();
                }

                if(curDepth == DEPTH_FOLDER && mParser.getName().equals("Placemark")) {

                    WhereCash whereCash = new WhereCash();
                    whereCash.name = findNextName();
                    whereCash.description = findNextDescription();
                    whereCash.images = findExtendedData();
                    whereCash.latLng = findLatLng();

                    curFolder.list.add(whereCash);
                }
            }

            if(eventType == EVENT_TYPE_TAG_CLOSE) {
                if(curDepth == DEPTH_FOLDER && mParser.getName().equals("Folder")) {
                    curDepth = DEPTH_DOCUMENT;

                    if(curFolder != null) {
                        arrFolders.add(curFolder);
                        curFolder = null;
                    }
                }

                if(curDepth == DEPTH_DOCUMENT && mParser.getName().equals("Document")) {
                    curDepth = DEPTH_KML;
                }

                if(curDepth == DEPTH_KML && mParser.getName().equals("kml")) {
                    curDepth = DEPTH_ZERO;
                }
            }
        }
    }

    private LatLng findLatLng() throws XmlPullParserException, IOException{
        LatLng latLng = null;
        int depth = 0;
        for(int eventType = mParser.getEventType(); eventType != 1;  eventType = mParser.next()) {
            if(eventType == EVENT_TYPE_TAG_OPEN) {
                if(mParser.getName().equals("Point")) {
                    depth = 1;
                }

                if(depth == 1 && mParser.getName().equals("coordinates")) {
                    findText();
                    String temp = mParser.getText();
                    findClose();

                    String strLongitude = temp.substring(0, temp.indexOf(",")).trim();
                    String strLatitude = temp.substring(temp.indexOf(",") + 1).trim();
                    strLatitude = strLatitude.substring(0, strLatitude.indexOf(","));

                    latLng = new LatLng(Double.parseDouble(strLatitude), Double.parseDouble(strLongitude));
                }
            }

            if(eventType == EVENT_TYPE_TAG_CLOSE) {
                if(depth == 1 && mParser.getName().equals("Point")) {
                    break;
                }
            }
        }
        return latLng;
    }

    private ArrayList<String> findExtendedData() throws XmlPullParserException, IOException {
        ArrayList<String > ret = new ArrayList<>();
        int depth = 0;
        for(int eventType = mParser.getEventType(); eventType != 1;  eventType = mParser.next()) {
            if(eventType == EVENT_TYPE_TAG_OPEN) {

                if(mParser.getName().equals("ExtendedData")) {
                    depth = 1;
                }

                if(depth == 1 && mParser.getName().equals("value")) {
                    findText();
                    ret.add(mParser.getText());
                    findClose();
                }
            }

            if(eventType == EVENT_TYPE_TAG_CLOSE) {
                if(depth == 1 && mParser.getName().equals("ExtendedData")) {
                    break;
                }
            }
        }
        return ret;
    }

    private String findNextName()  throws XmlPullParserException, IOException {
        String ret = null;
        for(int eventType = mParser.getEventType(); eventType != 1; eventType = mParser.next()) {
            if (eventType == EVENT_TYPE_TAG_OPEN) {
                if (mParser.getName().equals("name")) {
                    findText();
                    ret = mParser.getText();
                    findClose();
                    return ret;
                }
            }
        }
        return null;
    }

    private String findNextDescription()  throws XmlPullParserException, IOException {
        String ret = null;
        for(int eventType = mParser.getEventType(); eventType != 1; eventType = mParser.next()) {
            if (eventType == EVENT_TYPE_TAG_OPEN) {
                if (mParser.getName().equals("description")) {
                    findText();
                    ret = mParser.getText();
                    findClose();
                    return ret;
                }
            }
        }
        return null;
    }

    private void findNameAndDescriptionOfDocument() throws XmlPullParserException, IOException  {
        for(int eventType = mParser.getEventType(); eventType != 1; eventType = mParser.next()) {
            if(eventType == EVENT_TYPE_TAG_OPEN) {
                if(mParser.getName().equals("name")) {
                    findText();
                    name = mParser.getText();
                    findClose();
                } else if(mParser.getName().equals("description")) {
                    findText();
                    description = mParser.getText();
                    findClose();
                    return;
                }
            }
        }
    }

    private void findText() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while(eventType != EVENT_TYPE_TAG_TEXT) {
            eventType = mParser.next();
        }
    }

    private void findClose() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while(eventType != EVENT_TYPE_TAG_CLOSE) {
            eventType = mParser.next();
        }
    }

    /**
     * 해당 Tag는 건너 뛴다.
     */
    private void skip() throws XmlPullParserException, IOException {
        if (mParser.getEventType() != EVENT_TYPE_TAG_OPEN) {
            throw new IllegalStateException();
        } else {
            int depth = 1;

            while(depth != 0) {
                switch(mParser.next()) {
                    case EVENT_TYPE_TAG_OPEN:
                        ++depth;
                        break;
                    case EVENT_TYPE_TAG_CLOSE:
                        --depth;
                }
            }

        }
    }
}
