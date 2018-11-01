package jrkim.rcash.consts;

public class RCashConsts {
    /**
     * TODO List
     *
     * 1. 특정 종이지갑에서 인출 안되는 이슈 확인 (utxo에 차이점이 있다면 scriptPubkey 에 이미 값이 있다는거? 이거 언제생기는거지?)
     * 2. 상대방 특정 Address로 QR코드 만들어 공유해주기 기능 추가
     * 3. 지문 잠금 지원
     * 4. Where.Cash 카테고리 별로 지정해서 보기 기능 추가?
     * 5. QRCode 생성 기능 QART 앱처럼 개선 가능할까? 다양하게????
     */

    /**
     * Free Fonts
     */
    public static final String FONT_NOTO_BOLD = "fonts/Noto/NotoSansKR-Bold-Hestia.otf";
    public static final String FONT_NOTO_MEDIUM = "fonts/Noto/NotoSansKR-Medium-Hestia.otf";
    public static final String FONT_NOTO_REGULAR = "fonts/Noto/NotoSansKR-Regular-Hestia.otf";
    public static final String FONT_NOTO_LIGHT = "fonts/Noto/NotoSansKR-Light-Hestia.otf";
    public static final String FONT_NOTO_THIN = "fonts/Noto/NotoSansKR-Thin-Hestia.otf";

    public static final String FONT_NANUM_SQUARE_B = "fonts/NanumSquare/NanumSquareB.otf";
    public static final String FONT_NANUM_SQUARE_R = "fonts/NanumSquare/NanumSquareR.otf";

    public static final String FONT_SANSATION_BOLD = "fonts/sansation/Sansation-Bold.ttf";
    public static final String FONT_SANSATION_BOLD_ITALIC = "fonts/sansation/Sansation-BoldItalic.ttf";
    public static final String FONT_SANSATION_ITALIC = "fonts/sansation/Sansation-Italic.ttf";
    public static final String FONT_SANSATION_LIGHT = "fonts/sansation/Sansation-Light.ttf";
    public static final String FONT_SANSATION_LIGHT_ITALIC = "fonts/sansation/Sansation-LightItalic.ttf";
    public static final String FONT_SANSATION_REGULAR = "fonts/sansation/Sansation-Regular.ttf";

    public static final String FONT_UBUNTU_REGULAR = "fonts/Ubuntu/Ubuntu-Regular.ttf";
    public static final String FONT_UBUNTU_MEDIUM_ITALIC = "fonts/Ubuntu/Ubuntu-MediumItalic.ttf";
    public static final String FONT_UBUNTU_MEDIUM = "fonts/Ubuntu/Ubuntu-Medium.ttf";
    public static final String FONT_UBUNTU_LIGHT_ITALIC = "fonts/Ubuntu/Ubuntu-LightItalic.ttf";
    public static final String FONT_UBUNTU_LIGHT = "fonts/Ubuntu/Ubuntu-Light.ttf";
    public static final String FONT_UBUNTU_ITALIC = "fonts/Ubuntu/Ubuntu-Italic.ttf";
    public static final String FONT_UBUNTU_BOLD_ITALIC = "fonts/Ubuntu/Ubuntu-BoldItalic.ttf";
    public static final String FONT_UBUNTU_BOLD = "fonts/Ubuntu/Ubuntu-Bold.ttf";

    public static final String FONT_INK_BURRO = "fonts/inkburro/inkburro.ttf";

    /**
     * Networking states
     */
    public static final int NETWORK_NOTCONNECTED = 0;
    public static final int NETWORK_MOBILE = 1;
    public static final int NETWORK_WIFI = 2;

    /**
     * SharedPreferences
     */
    public final static String SHAREDPREF_INT_DBVERSION = "sharedpref_int_dbversion";       //현재 app의 db 버전
    public final static String SHAREDPREF_INT_EDIT_QRCODE = "sharedpref_int_edit_qrcode";   // QR코드 수정 기능 안내 횟수 (3회)
    public final static String SHAREDPREF_INT_QRCODE_DOT_SIZE = "sharedpref_int_qrcode_dot_size"; // QR코드 도트 사이즈
    public final static String SHAREDPREF_INT_BCH_FORMAT = "sharedpref_int_bch_format"; // BCH 단위
    public final static String SHAREDPREF_INT_LOCAL_CURRENCY = "sharedpref_int_local_currency"; // 지역 법정 화폐
    public final static String SHAREDPREF_INT_WALLET_COLOR = "sharedpref_int_wallet_color"; //지갑 색상
    public final static String SHAREDPREF_INT_ADDRESS_FORMAT = "sharedpref_int_address_format"; // 주소 포멧
    public final static String SHAREDPREF_INT_READ_QRCODE_FROM_IMAGEFILE = "sharedpref_int_read_qrcode_from_imagefile"; //이미지로 부터 qr코드 읽기 안내 횟수
    public final static String SHAREDPREF_INT_CROP_LEFT = "sharedpref_int_crop_left";   // gif crop position left
    public final static String SHAREDPREF_INT_CROP_RIGHT = "sharedpref_int_crop_right"; // gif crop position right
    public final static String SHAREDPREF_INT_CROP_TOP = "sharedpref_int_crop_top";     // gif crop position top
    public final static String SHAREDPREF_INT_CROP_BOTTOM = "sharedpref_int_crop_bottom";// gif crop position bottom

    public final static String SHAREDPREF_BOOL_EULA_ACCEPTED = "sharedpref_bool_eula_accepted"; // EULA 동의
    public final static String SHAREDPREF_BOOL_SECUREMODE = "sharedpref_bool_securemode";   //화면잠금사용
    public final static String SHAREDPREF_BOOL_WALLET_CREATED = "sharedpref_bool_wallet_created"; // 지갑 생성여부
    public final static String SHAREDPREF_BOOL_BACKUP = "sharedpref_bool_backup"; //백업 프레이즈 확인 여부
    public final static String SHAREDPREF_BOOL_SYNC_COMPLETED = "sharedpref_bool_sync_completed"; // 블록체인 동기화 여부
    public final static String SHAREDPREF_BOOL_FINGERPRINT = "sharedpref_bool_fingerprint"; // 지문잠금 사용 여부

    public final static String SHAREDPREF_STRING_SECURE_PASSWORD = "sharedpref_string_secure_password"; //앱 잠금
    public final static String SHAREDPREF_STRING_WALLET_ALIAS = "sharedpref_string_wallet_alias"; // 앱 별명
    public final static String SHAREDPREF_STRING_LAST_ADDRESS = "sharedpref_string_last_address"; // 마지막으로 사용한 공개키 주소

    public final static String SHAREDPREF_LONG_LAST_WHERECASH_DOWNLOAD = "sharedpref_long_last_wherecash_download"; // WhereCash 다운로드 시간

    public final static int BCH_FORMAT_BCH = 0;
    public final static int BCH_FORMAT_BITS = 1;
    public final static int BCH_FORMAT_SATOSHIS = 2;

    public final static int LOCAL_CURRENCY_KRW = 0;
    public final static int LOCAL_CURRENCY_USD = 1;
    public final static int LOCAL_CURRENCY_EU = 2;
    public final static int LOCAL_CURRENCY_JPY = 3;
    public final static int LOCAL_CURRENCY_CHY = 4;

    public final static int WALLET_COLOR_RED = 0;
    public final static int WALLET_COLOR_PINK = 1;
    public final static int WALLET_COLOR_ORANGE = 2;
    public final static int WALLET_COLOR_PURPLE = 3;
    public final static int WALLET_COLOR_INDIGO = 4;
    public final static int WALLET_COLOR_CYON = 5;
    public final static int WALLET_COLOR_GREEN = 6;
    public final static int WALLET_COLOR_BROWN = 7;
    public final static int WALLET_COLOR_BLUEGREY = 8;

    public final static int ADDRESS_FORMAT_LEGACY = 0;      // start with 1
    public final static int ADDRESS_FORMAT_CASHADDR = 1;    // start with q  or  bitcoincash:/

    /**
     * RequestCodes
     */
    public final static int REQCODE_PINCODE = 100;
    public final static int REQCODE_EULA = 101;
    public final static int REQCODE_QRCODE = 102;
    public final static int REQCODE_PINCODE_SET = 103;
    public final static int REQCODE_PINCODE_REMOVE = 104;
    public final static int REQCODE_BACKUP = 105;
    public final static int REQCODE_RESTORE = 106;
    public final static int REQCODE_CHECK = 107;
    public final static int REQCODE_SEND = 108;
    public final static int REQCODE_PHOTOSELECTOR= 109;
    public final static int REQCODE_CROPIMAGE = 110;
    public final static int REQCODE_EDITWALLET = 111;
    public final static int REQCODE_MYADDRESS = 112;

    /**
     * Results
     */
    public final static int RESULT_GIF = 1000;


    /**
     * extras
     */
    public final static String EXTRA_ADDRESS = "extra_address";
    public final static String EXTRA_AMOUNT = "extra_amount";
    public final static String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_SEED = "extra_seed";
    public static final String EXTRA_REQCODE = "extra_reqcode";
    public static final String EXTRA_SATOSHIS = "extra_satoshis";
    public static final String EXTRA_CREATION_TIME = "extra_creationtime";
    public static final String EXTRA_BIP38PRIKEY = "extra_bip38prikey";
    public static final String EXTRA_BIP38PASSWORD = "extra_password";
    public static final String EXTRA_BIP38CREATIONTIME = "extra_bip38creationtime";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_PURPOSE = "extra_purpose";
    public static final String EXTRA_LEFT = "extra_left";
    public static final String EXTRA_RIGHT = "extra_right";
    public static final String EXTRA_TOP = "extra_top";
    public static final String EXTRA_BOTTOM = "extra_bottom";
    public static final String EXTRA_FILEPATH = "extra_filepath";

    /**
     * RequestPermissionCodes
     */
    public final static int PERMISSION_RQCODE_CAMERA = 200;

    /**
     * Messages
     * from 1000~1999 = 각 앱내 내부 메세지로 사용
     */
    // Pincode
    public final static int BROADCAST_PINCODE_WILL_APPEAR = 2000;

    // BitcoinService
    public final static int BROADCAST_BITCOINSERVICE_CONNECT_RESULT = 3100;
    public final static int BROADCAST_BITCOINSERVICE_DISCONNECT_RESULT = 3101;
    public final static int BROADCAST_BITCOINSERVICE_SEND_RESULT = 3102;
    public final static int BROADCAST_BITCOINSERVICE_CHECK = 3103;
    public final static int BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS = 3104;
    public final static int BROADCAST_BITCOINSERVICE_BLOCKCHAIN_PROGRESS_BIP38 = 3105;

    public final static int BROADCAST_BITCOINSERVICE_CREATE_NEW_WALLET_AND_SUBSCRIPT_BLOCKCHAIN = 3200;
    public final static int BROADCAST_BITCOINSERVICE_WALLET = 3201;
    public final static int BROADCAST_BITCOINSERVICE_CREATE_RESTORE_WALLET_WITH_SEED = 3202;
    public final static int BROADCAST_BITCOINSERVICE_FRESH_ADDRESS = 3203;
    public final static int BROADCAST_BITCOINSERVICE_RECEIVED = 3204;
    public final static int BROADCAST_BITCOINSERVICE_BIP38WALLET = 3205;

    public final static int BROADCAST_BITCOINSERVICE_BIP38_RESULT = 3300;

    // PlayStore
    public final static int BROADCAST_PLAYSTORE_VERSION = 4000;

    // Where.Cash
    public final static int BROADCAST_WHERECASH_COMPLETED = 5000;
    public final static int BROADCAST_WHERECASH_FAILED = 5001;

}
