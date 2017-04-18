package com.talmir.mickinet.activities;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.DBAction;
import com.talmir.mickinet.helpers.DividerItemDecoration;
import com.talmir.mickinet.helpers.IRecyclerClickListener;
import com.talmir.mickinet.helpers.MimeTypeAdapter;
import com.talmir.mickinet.helpers.MimeTypeSuggestionProvider;

import java.util.ArrayList;

public class SearchMimeTypeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MimeTypeAdapter mimeTypeAdapter;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_mime_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DBAction helper = new DBAction(getApplicationContext());

        db = helper.getWritableDatabase();

        Cursor mCursor = db.rawQuery("SELECT count(*) FROM mimeTypes;", null);
        mCursor.moveToFirst();
        if (mCursor.getInt(0) > 0)
            mCursor.close();
        else {
            ContentValues contentValues = new ContentValues();

            try {
                String[] mimeTypes = new String[662];
                mimeTypes[0] = "#1 application/acad (.dwg)";
                mimeTypes[1] = "#2 application/arj (.arj)";
                mimeTypes[2] = "#3 application/base64 (.mm)";
                mimeTypes[3] = "#4 application/base64 (.mme)";
                mimeTypes[4] = "#5 application/binhex (.hqx)";
                mimeTypes[5] = "#6 application/binhex4 (.hqx)";
                mimeTypes[6] = "#7 application/book (.boo)";
                mimeTypes[7] = "#8 application/book (.book)";
                mimeTypes[8] = "#9 application/cdf (.cdf)";
                mimeTypes[9] = "#10 application/clariscad (.ccad)";
                mimeTypes[10] = "#11 application/commonground (.dp)";
                mimeTypes[11] = "#12 application/drafting (.drw)";
                mimeTypes[12] = "#13 application/dsptype (.tsp)";
                mimeTypes[13] = "#14 application/dxf (.dxf)";
                mimeTypes[14] = "#15 application/ecmascript (.js)";
                mimeTypes[15] = "#16 application/envoy (.evy)";
                mimeTypes[16] = "#17 application/excel (.xl)";
                mimeTypes[17] = "#18 application/excel (.xla)";
                mimeTypes[18] = "#19 application/excel (.xlb)";
                mimeTypes[19] = "#20 application/excel (.xlc)";
                mimeTypes[20] = "#21 application/excel (.xld)";
                mimeTypes[21] = "#22 application/excel (.xlk)";
                mimeTypes[22] = "#23 application/excel (.xll)";
                mimeTypes[23] = "#24 application/excel (.xlm)";
                mimeTypes[24] = "#25 application/excel (.xls)";
                mimeTypes[25] = "#26 application/excel (.xlt)";
                mimeTypes[26] = "#27 application/excel (.xlv)";
                mimeTypes[27] = "#28 application/excel (.xlw)";
                mimeTypes[28] = "#29 application/fractals (.fif)";
                mimeTypes[29] = "#30 application/freeloader (.frl)";
                mimeTypes[30] = "#31 application/futuresplash (.spl)";
                mimeTypes[31] = "#32 application/gnutar (.tgz)";
                mimeTypes[32] = "#33 application/groupwise (.vew)";
                mimeTypes[33] = "#34 application/hlp (.hlp)";
                mimeTypes[34] = "#35 application/hta (.hta)";
                mimeTypes[35] = "#36 application/i-deas (.unv)";
                mimeTypes[36] = "#37 application/iges (.iges)";
                mimeTypes[37] = "#38 application/iges (.igs)";
                mimeTypes[38] = "#39 application/inf (.inf)";
                mimeTypes[39] = "#40 application/internet-property-stream (.acx)";
                mimeTypes[40] = "#41 application/java (.class)";
                mimeTypes[41] = "#42 application/java-byte-code (.class)";
                mimeTypes[42] = "#43 application/javascript (.js)";
                mimeTypes[43] = "#44 application/lha (.lha)";
                mimeTypes[44] = "#45 application/lzx (.lzx)";
                mimeTypes[45] = "#46 application/mac-binary (.bin)";
                mimeTypes[46] = "#47 application/mac-binhex (.hqx)";
                mimeTypes[47] = "#48 application/mac-binhex40 (.hqx)";
                mimeTypes[48] = "#49 application/mac-compactpro (.cpt)";
                mimeTypes[49] = "#50 application/macbinary (.bin)";
                mimeTypes[50] = "#51 application/marc (.mrc)";
                mimeTypes[51] = "#52 application/mbedlet (.mbd)";
                mimeTypes[52] = "#53 application/mcad (.mcd)";
                mimeTypes[53] = "#54 application/mime (.aps)";
                mimeTypes[54] = "#55 application/mspowerpoint (.pot)";
                mimeTypes[55] = "#56 application/mspowerpoint (.pps)";
                mimeTypes[56] = "#57 application/mspowerpoint (.ppt)";
                mimeTypes[57] = "#58 application/mspowerpoint (.ppz)";
                mimeTypes[58] = "#59 application/msword (.doc)";
                mimeTypes[59] = "#60 application/msword (.docx)";
                mimeTypes[60] = "#61 application/msword (.dot)";
                mimeTypes[61] = "#62 application/msword (.w6w)";
                mimeTypes[62] = "#63 application/msword (.wiz)";
                mimeTypes[63] = "#64 application/msword (.word)";
                mimeTypes[64] = "#65 application/mswrite (.wri)";
                mimeTypes[65] = "#66 application/netmc (.mcp)";
                mimeTypes[66] = "#67 application/octet-stream (.*)";
                mimeTypes[67] = "#68 application/octet-stream (.a)";
                mimeTypes[68] = "#69 application/octet-stream (.arc)";
                mimeTypes[69] = "#70 application/octet-stream (.arj)";
                mimeTypes[70] = "#71 application/octet-stream (.bin)";
                mimeTypes[71] = "#72 application/octet-stream (.class)";
                mimeTypes[72] = "#73 application/octet-stream (.com)";
                mimeTypes[73] = "#74 application/octet-stream (.dms)";
                mimeTypes[74] = "#75 application/octet-stream (.dump)";
                mimeTypes[75] = "#76 application/octet-stream (.exe)";
                mimeTypes[76] = "#77 application/octet-stream (.lha)";
                mimeTypes[77] = "#78 application/octet-stream (.lhx)";
                mimeTypes[78] = "#79 application/octet-stream (.lzh)";
                mimeTypes[79] = "#80 application/octet-stream (.lzx)";
                mimeTypes[80] = "#81 application/octet-stream (.o)";
                mimeTypes[81] = "#82 application/octet-stream (.psd)";
                mimeTypes[82] = "#83 application/octet-stream (.saveme)";
                mimeTypes[83] = "#84 application/octet-stream (.uu)";
                mimeTypes[84] = "#85 application/octet-stream (.zoo)";
                mimeTypes[85] = "#86 application/oda (.oda)";
                mimeTypes[86] = "#87 application/olescript (.axs)";
                mimeTypes[87] = "#88 application/pdf (.pdf)";
                mimeTypes[88] = "#89 application/pics-rules (.prf)";
                mimeTypes[89] = "#90 application/pkcs-crl (.crl)";
                mimeTypes[90] = "#91 application/pkcs7-mime (.p7c)";
                mimeTypes[91] = "#92 application/pkcs7-mime (.p7m)";
                mimeTypes[92] = "#93 application/pkcs7-signature (.p7s)";
                mimeTypes[93] = "#94 application/pkcs10 (.p10)";
                mimeTypes[94] = "#95 application/pkix-cert (.cer)";
                mimeTypes[95] = "#96 application/pkix-cert (.crt)";
                mimeTypes[96] = "#97 application/pkix-crl (.crl)";
                mimeTypes[97] = "#98 application/plain (.text)";
                mimeTypes[98] = "#99 application/postscript (.ai)";
                mimeTypes[99] = "#100 application/postscript (.eps)";
                mimeTypes[100] = "#101 application/postscript (.ps)";
                mimeTypes[101] = "#102 application/powerpoint (.ppt)";
                mimeTypes[102] = "#103 application/powerpoint (.pptx)";
                mimeTypes[103] = "#104 application/pro_eng (.part)";
                mimeTypes[104] = "#105 application/pro_eng (.prt)";
                mimeTypes[105] = "#106 application/ringing-tones (.rng)";
                mimeTypes[106] = "#107 application/rtf (.rtf)";
                mimeTypes[107] = "#108 application/rtf (.rtx)";
                mimeTypes[108] = "#109 application/sdp (.sdp)";
                mimeTypes[109] = "#110 application/sea (.sea)";
                mimeTypes[110] = "#111 application/set (.set)";
                mimeTypes[111] = "#112 application/sla (.stl)";
                mimeTypes[112] = "#113 application/smil (.smi)";
                mimeTypes[113] = "#114 application/smil (.smil)";
                mimeTypes[114] = "#115 application/solids (.sol)";
                mimeTypes[115] = "#116 application/sounder (.sdr)";
                mimeTypes[116] = "#117 application/step (.step)";
                mimeTypes[117] = "#118 application/step (.stp)";
                mimeTypes[118] = "#119 application/streamingmedia (.ssm)";
                mimeTypes[119] = "#120 application/toolbook (.tbk)";
                mimeTypes[120] = "#121 application/vda (.vda)";
                mimeTypes[121] = "#122 application/vnd.fdf (.fdf)";
                mimeTypes[122] = "#123 application/vnd.hp-hpgl (.hgl)";
                mimeTypes[123] = "#124 application/vnd.hp-hpgl (.hpg)";
                mimeTypes[124] = "#125 application/vnd.hp-hpgl (.hpgl)";
                mimeTypes[125] = "#126 application/vnd.hp-pcl (.pcl)";
                mimeTypes[126] = "#127 application/vnd.ms-excel (.xlb)";
                mimeTypes[127] = "#128 application/vnd.ms-excel (.xlc)";
                mimeTypes[128] = "#129 application/vnd.ms-excel (.xll)";
                mimeTypes[129] = "#130 application/vnd.ms-excel (.xlm)";
                mimeTypes[130] = "#131 application/vnd.ms-excel (.xls)";
                mimeTypes[131] = "#132 application/vnd.ms-excel (.xlt)";
                mimeTypes[132] = "#133 application/vnd.ms-excel (.xlw)";
                mimeTypes[133] = "#134 application/vnd.ms-excel (.xlsx)";
                mimeTypes[134] = "#135 application/vnd.ms-pki.certstore (.sst)";
                mimeTypes[135] = "#136 application/vnd.ms-pki.pko (.pko)";
                mimeTypes[136] = "#137 application/vnd.ms-pki.seccat (.cat)";
                mimeTypes[137] = "#138 application/vnd.ms-pki.stl (.stl)";
                mimeTypes[138] = "#139 application/vnd.ms-powerpoint (.pot)";
                mimeTypes[139] = "#140 application/vnd.ms-powerpoint (.ppa)";
                mimeTypes[140] = "#141 application/vnd.ms-powerpoint (.pps)";
                mimeTypes[141] = "#142 application/vnd.ms-powerpoint (.ppt)";
                mimeTypes[142] = "#143 application/vnd.ms-powerpoint (.pwz)";
                mimeTypes[143] = "#144 application/vnd.ms-project (.mpp)";
                mimeTypes[144] = "#145 application/vnd.nokia.configuration-message (.ncm)";
                mimeTypes[145] = "#146 application/vnd.nokia.ringing-tone (.rng)";
                mimeTypes[146] = "#147 application/vnd.rn-realmedia (.rm)";
                mimeTypes[147] = "#148 application/vnd.rn-realplayer (.rnx)";
                mimeTypes[148] = "#149 application/vnd.wap.wmlc (.wmlc)";
                mimeTypes[149] = "#150 application/vnd.wap.wmlscriptc (.wmlsc)";
                mimeTypes[150] = "#151 application/vnd.xara (.web)";
                mimeTypes[151] = "#152 application/vocaltec-media-desc (.vmd)";
                mimeTypes[152] = "#153 application/vocaltec-media-file (.vmf)";
                mimeTypes[153] = "#154 application/wordperfect (.wp)";
                mimeTypes[154] = "#155 application/wordperfect (.wp5)";
                mimeTypes[155] = "#156 application/wordperfect (.wp6)";
                mimeTypes[156] = "#157 application/wordperfect (.wpd)";
                mimeTypes[157] = "#158 application/wordperfect6.0 (.w60)";
                mimeTypes[158] = "#159 application/wordperfect6.0 (.wp5)";
                mimeTypes[159] = "#160 application/wordperfect6.1 (.w61)";
                mimeTypes[160] = "#161 application/x-123 (.wk1)";
                mimeTypes[161] = "#162 application/x-7z-compressed (.7z)";
                mimeTypes[162] = "#163 application/x-aim (.aim)";
                mimeTypes[163] = "#164 application/x-authorware-bin (.aab)";
                mimeTypes[164] = "#165 application/x-authorware-map (.aam)";
                mimeTypes[165] = "#166 application/x-authorware-seg (.aas)";
                mimeTypes[166] = "#167 application/x-bcpio (.bcpio)";
                mimeTypes[167] = "#168 application/x-binary (.bin)";
                mimeTypes[168] = "#169 application/x-binhex40 (.hqx)";
                mimeTypes[169] = "#170 application/x-bsh (.bsh)";
                mimeTypes[170] = "#171 application/x-bsh (.sh)";
                mimeTypes[171] = "#172 application/x-bsh (.shar)";
                mimeTypes[172] = "#173 application/x-bytecode.elisp (compiled elisp) (.elc)";
                mimeTypes[173] = "#174 application/x-bytecode.python (.pyc)";
                mimeTypes[174] = "#175 application/x-bzip (.bz)";
                mimeTypes[175] = "#176 application/x-bzip2 (.boz)";
                mimeTypes[176] = "#177 application/x-bzip2 (.bz2)";
                mimeTypes[177] = "#178 application/x-cdf (.cdf)";
                mimeTypes[178] = "#179 application/x-cdlink (.vcd)";
                mimeTypes[179] = "#180 application/x-chat (.cha)";
                mimeTypes[180] = "#181 application/x-chat (.chat)";
                mimeTypes[181] = "#182 application/x-cmu-raster (.ras)";
                mimeTypes[182] = "#183 application/x-cocoa (.cco)";
                mimeTypes[183] = "#184 application/x-compactpro (.cpt)";
                mimeTypes[184] = "#185 application/x-compress (.z)";
                mimeTypes[185] = "#186 application/x-compressed (.gz)";
                mimeTypes[186] = "#187 application/x-compressed (.tgz)";
                mimeTypes[187] = "#188 application/x-compressed (.z)";
                mimeTypes[188] = "#189 application/x-compressed (.zip)";
                mimeTypes[189] = "#190 application/x-conference (.nsc)";
                mimeTypes[190] = "#191 application/x-cpio (.cpio)";
                mimeTypes[191] = "#192 application/x-cpt (.cpt)";
                mimeTypes[192] = "#193 application/x-csh (.csh)";
                mimeTypes[193] = "#194 application/x-deepv (.deepv)";
                mimeTypes[194] = "#195 application/x-director (.dcr)";
                mimeTypes[195] = "#196 application/x-director (.dir)";
                mimeTypes[196] = "#197 application/x-director (.dxr)";
                mimeTypes[197] = "#198 application/x-dvi (.dvi)";
                mimeTypes[198] = "#199 application/x-elc (.elc)";
                mimeTypes[199] = "#200 application/x-envoy (.env)";
                mimeTypes[200] = "#201 application/x-envoy (.evy)";
                mimeTypes[201] = "#202 application/x-esrehber (.es)";
                mimeTypes[202] = "#203 application/x-excel (.xla)";
                mimeTypes[203] = "#204 application/x-excel (.xlb)";
                mimeTypes[204] = "#205 application/x-excel (.xlc)";
                mimeTypes[205] = "#206 application/x-excel (.xld)";
                mimeTypes[206] = "#207 application/x-excel (.xlk)";
                mimeTypes[207] = "#208 application/x-excel (.xll)";
                mimeTypes[208] = "#209 application/x-excel (.xlm)";
                mimeTypes[209] = "#210 application/x-excel (.xls)";
                mimeTypes[210] = "#211 application/x-excel (.xlt)";
                mimeTypes[211] = "#212 application/x-excel (.xlv)";
                mimeTypes[212] = "#213 application/x-excel (.xlw)";
                mimeTypes[213] = "#214 application/x-frame (.mif)";
                mimeTypes[214] = "#215 application/x-freelance (.pre)";
                mimeTypes[215] = "#216 application/x-gsp (.gsp)";
                mimeTypes[216] = "#217 application/x-gss (.gss)";
                mimeTypes[217] = "#218 application/x-gtar (.gtar)";
                mimeTypes[218] = "#219 application/x-gzip (.gz)";
                mimeTypes[219] = "#220 application/x-gzip (.gzip)";
                mimeTypes[220] = "#221 application/x-hdf (.hdf)";
                mimeTypes[221] = "#222 application/x-helpfile (.help)";
                mimeTypes[222] = "#223 application/x-helpfile (.hlp)";
                mimeTypes[223] = "#224 application/x-httpd-imap (.imap)";
                mimeTypes[224] = "#225 application/x-ima (.ima)";
                mimeTypes[225] = "#226 application/x-internett-signup (.ins)";
                mimeTypes[226] = "#227 application/x-inventor (.iv)";
                mimeTypes[227] = "#228 application/x-ip2 (.ip)";
                mimeTypes[228] = "#229 application/x-java-class (.class)";
                mimeTypes[229] = "#230 application/x-java-commerce (.jcm)";
                mimeTypes[230] = "#231 application/x-javascript (.js)";
                mimeTypes[231] = "#232 application/x-koan (.skd)";
                mimeTypes[232] = "#233 application/x-koan (.skm)";
                mimeTypes[233] = "#234 application/x-koan (.skp)";
                mimeTypes[234] = "#235 application/x-koan (.skt)";
                mimeTypes[235] = "#236 application/x-ksh (.ksh)";
                mimeTypes[236] = "#237 application/x-latex (.latex)";
                mimeTypes[237] = "#238 application/x-latex (.ltx)";
                mimeTypes[238] = "#239 application/x-lha (.lha)";
                mimeTypes[239] = "#240 application/x-lisp (.lsp)";
                mimeTypes[240] = "#241 application/x-livescreen (.ivy)";
                mimeTypes[241] = "#242 application/x-lotus (.wq1)";
                mimeTypes[242] = "#243 application/x-lotusscreencam (.scm)";
                mimeTypes[243] = "#244 application/x-lzh (.lzh)";
                mimeTypes[244] = "#245 application/x-lzx (.lzx)";
                mimeTypes[245] = "#246 application/x-mac-binhex40 (.hqx)";
                mimeTypes[246] = "#247 application/x-macbinary (.bin)";
                mimeTypes[247] = "#248 application/x-magic-cap-package-1.0 (.mc$)";
                mimeTypes[248] = "#249 application/x-mathcad (.mcd)";
                mimeTypes[249] = "#250 application/x-meme (.mm)";
                mimeTypes[250] = "#251 application/x-midi (.mid)";
                mimeTypes[251] = "#252 application/x-midi (.midi)";
                mimeTypes[252] = "#253 application/x-mif (.mif)";
                mimeTypes[253] = "#254 application/x-mix-transfer (.nix)";
                mimeTypes[254] = "#255 application/x-mplayer2 (.asx)";
                mimeTypes[255] = "#256 application/x-msexcel (.xla)";
                mimeTypes[256] = "#257 application/x-msexcel (.xls)";
                mimeTypes[257] = "#258 application/x-msexcel (.xlw)";
                mimeTypes[258] = "#259 application/x-mspowerpoint (.ppt)";
                mimeTypes[259] = "#260 application/x-navi-animation (.ani)";
                mimeTypes[260] = "#261 application/x-navidoc (.nvd)";
                mimeTypes[261] = "#262 application/x-navimap (.map)";
                mimeTypes[262] = "#263 application/x-navistyle (.stl)";
                mimeTypes[263] = "#264 application/x-netcdf (.cdf)";
                mimeTypes[264] = "#265 application/x-netcdf (.nc)";
                mimeTypes[265] = "#266 application/x-newton-compatible-pkg (.pkg)";
                mimeTypes[266] = "#267 application/x-nokia-9000-communicator-add-on-software (.aos)";
                mimeTypes[267] = "#268 application/x-msaccess (.mdb)";
                mimeTypes[268] = "#269 application/x-msdownload (.dll)";
                mimeTypes[269] = "#270 application/x-omc (.omc)";
                mimeTypes[270] = "#271 application/x-omcdatamaker (.omcd)";
                mimeTypes[271] = "#272 application/x-omcregerator (.omcr)";
                mimeTypes[272] = "#273 application/x-pagemaker (.pm4)";
                mimeTypes[273] = "#274 application/x-pagemaker (.pm5)";
                mimeTypes[274] = "#275 application/x-pcl (.pcl)";
                mimeTypes[275] = "#276 application/x-pixclscript (.plx)";
                mimeTypes[276] = "#277 application/x-pkcs7-certificates (.spc)";
                mimeTypes[277] = "#278 application/x-pkcs7-certreqresp (.p7r)";
                mimeTypes[278] = "#279 application/x-pkcs7-mime (.p7c)";
                mimeTypes[279] = "#280 application/x-pkcs7-mime (.p7m)";
                mimeTypes[280] = "#281 application/x-pkcs7-signature (.p7a)";
                mimeTypes[281] = "#282 application/x-pkcs10 (.p10)";
                mimeTypes[282] = "#283 application/x-pkcs12 (.p12)";
                mimeTypes[283] = "#284 application/x-pointplus (.css)";
                mimeTypes[284] = "#285 application/x-portable-anymap (.pnm)";
                mimeTypes[285] = "#286 application/x-project (.mpc)";
                mimeTypes[286] = "#287 application/x-project (.mpt)";
                mimeTypes[287] = "#288 application/x-project (.mpv)";
                mimeTypes[288] = "#289 application/x-project (.mpx)";
                mimeTypes[289] = "#290 application/x-qpro (.wb1)";
                mimeTypes[290] = "#291 application/x-rar-compressed (.rar)";
                mimeTypes[291] = "#292 application/x-rtf (.rtf)";
                mimeTypes[292] = "#293 application/x-sdp (.sdp)";
                mimeTypes[293] = "#294 application/x-sea (.sea)";
                mimeTypes[294] = "#295 application/x-seelogo (.sl)";
                mimeTypes[295] = "#296 application/x-sh (.sh)";
                mimeTypes[296] = "#297 application/x-shar (.sh)";
                mimeTypes[297] = "#298 application/x-shar (.shar)";
                mimeTypes[298] = "#299 application/x-shockwave-flash (.swf)";
                mimeTypes[299] = "#300 application/x-sit (.sit)";
                mimeTypes[300] = "#301 application/x-sprite (.spr)";
                mimeTypes[301] = "#302 application/x-sprite (.sprite)";
                mimeTypes[302] = "#303 application/x-stuffit (.sit)";
                mimeTypes[303] = "#304 application/x-sv4cpio (.sv4cpio)";
                mimeTypes[304] = "#305 application/x-sv4crc (.sv4crc)";
                mimeTypes[305] = "#306 application/x-tar (.tar)";
                mimeTypes[306] = "#307 application/x-tbook (.sbk)";
                mimeTypes[307] = "#308 application/x-tbook (.tbk)";
                mimeTypes[308] = "#309 application/x-tcl (.tcl)";
                mimeTypes[309] = "#310 application/x-tex (.tex)";
                mimeTypes[310] = "#311 application/x-texinfo (.texi)";
                mimeTypes[311] = "#312 application/x-texinfo (.texinfo)";
                mimeTypes[312] = "#313 application/x-troff (.roff)";
                mimeTypes[313] = "#314 application/x-troff (.t)";
                mimeTypes[314] = "#315 application/x-troff (.tr)";
                mimeTypes[315] = "#316 application/x-troff-man (.man)";
                mimeTypes[316] = "#317 application/x-troff-me (.me)";
                mimeTypes[317] = "#318 application/x-troff-ms (.ms)";
                mimeTypes[318] = "#319 application/x-troff-msvideo (.avi)";
                mimeTypes[319] = "#320 application/x-ustar (.ustar)";
                mimeTypes[320] = "#321 application/x-visio (.vsd)";
                mimeTypes[321] = "#322 application/x-visio (.vst)";
                mimeTypes[322] = "#323 application/x-visio (.vsw)";
                mimeTypes[323] = "#324 application/x-vnd.audioexplosion.mzz (.mzz)";
                mimeTypes[324] = "#325 application/x-vnd.ls-xpix (.xpix)";
                mimeTypes[325] = "#326 application/x-vrml (.vrml)";
                mimeTypes[326] = "#327 application/x-wais-source (.src)";
                mimeTypes[327] = "#328 application/x-wais-source (.wsrc)";
                mimeTypes[328] = "#329 application/x-winhelp (.hlp)";
                mimeTypes[329] = "#330 application/x-wintalk (.wtk)";
                mimeTypes[330] = "#331 application/x-world (.svr)";
                mimeTypes[331] = "#332 application/x-world (.wrl)";
                mimeTypes[332] = "#333 application/x-wpwin (.wpd)";
                mimeTypes[333] = "#334 application/x-wri (.wri)";
                mimeTypes[334] = "#335 application/x-x509-ca-cert (.cer)";
                mimeTypes[335] = "#336 application/x-x509-ca-cert (.crt)";
                mimeTypes[336] = "#337 application/x-x509-ca-cert (.der)";
                mimeTypes[337] = "#338 application/x-x509-user-cert (.crt)";
                mimeTypes[338] = "#339 application/x-zip-compressed (.zip)";
                mimeTypes[339] = "#340 application/xml (.xml)";
                mimeTypes[340] = "#341 application/zip (.zip)";
                mimeTypes[341] = "#342 audio/aiff (.aif)";
                mimeTypes[342] = "#343 audio/aiff (.aifc)";
                mimeTypes[343] = "#344 audio/aiff (.aiff)";
                mimeTypes[344] = "#345 audio/basic (.au)";
                mimeTypes[345] = "#346 audio/basic (.snd)";
                mimeTypes[346] = "#347 audio/it (.it)";
                mimeTypes[347] = "#348 audio/make (.funk)";
                mimeTypes[348] = "#349 audio/make (.my)";
                mimeTypes[349] = "#350 audio/make (.pfunk)";
                mimeTypes[350] = "#351 audio/make.my.funk (.pfunk)";
                mimeTypes[351] = "#352 audio/mid (.rmi)";
                mimeTypes[352] = "#353 audio/midi (.kar)";
                mimeTypes[353] = "#354 audio/midi (.mid)";
                mimeTypes[354] = "#355 audio/midi (.midi)";
                mimeTypes[355] = "#356 audio/mod (.mod)";
                mimeTypes[356] = "#357 audio/mpeg (.m2a)";
                mimeTypes[357] = "#358 audio/mpeg (.mp2)";
                mimeTypes[358] = "#359 audio/mpeg (.mp3)";
                mimeTypes[359] = "#360 audio/mpeg (.mpa)";
                mimeTypes[360] = "#361 audio/mpeg (.mpg)";
                mimeTypes[361] = "#362 audio/mpeg (.mpga)";
                mimeTypes[362] = "#363 audio/mpeg3 (.mp3)";
                mimeTypes[363] = "#364 audio/nspaudio (.la)";
                mimeTypes[364] = "#365 audio/nspaudio (.lma)";
                mimeTypes[365] = "#366 audio/s3m (.s3m)";
                mimeTypes[366] = "#367 audio/tsp-audio (.tsi)";
                mimeTypes[367] = "#368 audio/tsplayer (.tsp)";
                mimeTypes[368] = "#369 audio/vnd.qcelp (.qcp)";
                mimeTypes[369] = "#370 audio/voc (.voc)";
                mimeTypes[370] = "#371 audio/voxware (.vox)";
                mimeTypes[371] = "#372 audio/wav (.wav)";
                mimeTypes[372] = "#373 audio/x-adpcm (.snd)";
                mimeTypes[373] = "#374 audio/x-aiff (.aif)";
                mimeTypes[374] = "#375 audio/x-aiff (.aifc)";
                mimeTypes[375] = "#376 audio/x-aiff (.aiff)";
                mimeTypes[376] = "#377 audio/x-au (.au)";
                mimeTypes[377] = "#378 audio/x-gsm (.gsd)";
                mimeTypes[378] = "#379 audio/x-gsm (.gsm)";
                mimeTypes[379] = "#380 audio/x-jam (.jam)";
                mimeTypes[380] = "#381 audio/x-liveaudio (.lam)";
                mimeTypes[381] = "#382 audio/x-mid (.mid)";
                mimeTypes[382] = "#383 audio/x-mid (.midi)";
                mimeTypes[383] = "#384 audio/x-midi (.mid)";
                mimeTypes[384] = "#385 audio/x-midi (.midi)";
                mimeTypes[385] = "#386 audio/x-mod (.mod)";
                mimeTypes[386] = "#387 audio/x-mpeg (.mp2)";
                mimeTypes[387] = "#388 audio/x-mpeg-3 (.mp3)";
                mimeTypes[388] = "#389 audio/x-mpequrl (.m3u)";
                mimeTypes[389] = "#390 audio/x-nspaudio (.la)";
                mimeTypes[390] = "#391 audio/x-nspaudio (.lma)";
                mimeTypes[391] = "#392 audio/x-pn-realaudio (.ra)";
                mimeTypes[392] = "#393 audio/x-pn-realaudio (.ram)";
                mimeTypes[393] = "#394 audio/x-pn-realaudio (.rm)";
                mimeTypes[394] = "#395 audio/x-pn-realaudio (.rmm)";
                mimeTypes[395] = "#396 audio/x-pn-realaudio (.rmp)";
                mimeTypes[396] = "#397 audio/x-pn-realaudio-plugin (.ra)";
                mimeTypes[397] = "#398 audio/x-pn-realaudio-plugin (.rmp)";
                mimeTypes[398] = "#399 audio/x-pn-realaudio-plugin (.rpm)";
                mimeTypes[399] = "#400 audio/x-psid (.sid)";
                mimeTypes[400] = "#401 audio/x-realaudio (.ra)";
                mimeTypes[401] = "#402 audio/x-twinvq (.vqf)";
                mimeTypes[402] = "#403 audio/x-twinvq-plugin (.vqe)";
                mimeTypes[403] = "#404 audio/x-twinvq-plugin (.vql)";
                mimeTypes[404] = "#405 audio/x-vnd.audioexplosion.mjuicemediafile (.mjf)";
                mimeTypes[405] = "#406 audio/x-voc (.voc)";
                mimeTypes[406] = "#407 audio/x-wav (.wav)";
                mimeTypes[407] = "#408 audio/xm (.xm)";
                mimeTypes[408] = "#409 chemical/x-pdb (.pdb)";
                mimeTypes[409] = "#410 chemical/x-pdb (.xyz)";
                mimeTypes[410] = "#411 drawing/x-dwf (old) (.dwf)";
                mimeTypes[411] = "#412 i-world/i-vrml (.ivr)";
                mimeTypes[412] = "#413 image/bmp (.bm)";
                mimeTypes[413] = "#414 image/bmp (.bmp)";
                mimeTypes[414] = "#415 image/cmu-raster (.ras)";
                mimeTypes[415] = "#416 image/cmu-raster (.rast)";
                mimeTypes[416] = "#417 image/fif (.fif)";
                mimeTypes[417] = "#418 image/florian (.flo)";
                mimeTypes[418] = "#419 image/florian (.turbot)";
                mimeTypes[419] = "#420 image/g3fax (.g3)";
                mimeTypes[420] = "#421 image/gif (.gif)";
                mimeTypes[421] = "#422 image/ief (.ief)";
                mimeTypes[422] = "#423 image/ief (.iefs)";
                mimeTypes[423] = "#424 image/jpeg (.jfif)";
                mimeTypes[424] = "#425 image/jpeg (.jfif-tbnl)";
                mimeTypes[425] = "#426 image/jpeg (.jpe)";
                mimeTypes[426] = "#427 image/jpeg (.jpeg)";
                mimeTypes[427] = "#428 image/jpeg (.jpg)";
                mimeTypes[428] = "#429 image/jutvision (.jut)";
                mimeTypes[429] = "#430 image/naplps (.nap)";
                mimeTypes[430] = "#431 image/naplps (.naplps)";
                mimeTypes[431] = "#432 image/pict (.pic)";
                mimeTypes[432] = "#433 image/pict (.pict)";
                mimeTypes[433] = "#434 image/pjpeg (.jfif)";
                mimeTypes[434] = "#435 image/pjpeg (.jpe)";
                mimeTypes[435] = "#436 image/pjpeg (.jpeg)";
                mimeTypes[436] = "#437 image/pjpeg (.jpg)";
                mimeTypes[437] = "#438 image/png (.png)";
                mimeTypes[438] = "#439 image/png (.x-png)";
                mimeTypes[439] = "#440 image/tiff (.tif)";
                mimeTypes[440] = "#441 image/tiff (.tiff)";
                mimeTypes[441] = "#442 image/vasa (.mcf)";
                mimeTypes[442] = "#443 image/vnd.dwg (.dwg)";
                mimeTypes[443] = "#444 image/vnd.dwg (.dxf)";
                mimeTypes[444] = "#445 image/vnd.dwg (.svf)";
                mimeTypes[445] = "#446 image/vnd.fpx (.fpx)";
                mimeTypes[446] = "#447 image/vnd.net-fpx (.fpx)";
                mimeTypes[447] = "#448 image/vnd.rn-realflash (.rf)";
                mimeTypes[448] = "#449 image/vnd.rn-realpix (.rp)";
                mimeTypes[449] = "#450 image/vnd.wap.wbmp (.wbmp)";
                mimeTypes[450] = "#451 image/vnd.xiff (.xif)";
                mimeTypes[451] = "#452 image/x-cmu-raster (.ras)";
                mimeTypes[452] = "#453 image/x-dwg (.dwg)";
                mimeTypes[453] = "#454 image/x-dwg (.dxf)";
                mimeTypes[454] = "#455 image/x-dwg (.svf)";
                mimeTypes[455] = "#456 image/x-icon (.ico)";
                mimeTypes[456] = "#457 image/x-jg (.art)";
                mimeTypes[457] = "#458 image/x-jps (.jps)";
                mimeTypes[458] = "#459 image/x-niff (.nif)";
                mimeTypes[459] = "#460 image/x-niff (.niff)";
                mimeTypes[460] = "#461 image/x-pcx (.pcx)";
                mimeTypes[461] = "#462 image/x-pict (.pct)";
                mimeTypes[462] = "#463 image/x-portable-anymap (.pnm)";
                mimeTypes[463] = "#464 image/x-portable-bitmap (.pbm)";
                mimeTypes[464] = "#465 image/x-portable-graymap (.pgm)";
                mimeTypes[465] = "#466 image/x-portable-greymap (.pgm)";
                mimeTypes[466] = "#467 image/x-portable-pixmap (.ppm)";
                mimeTypes[467] = "#468 image/x-quicktime (.qif)";
                mimeTypes[468] = "#469 image/x-quicktime (.qti)";
                mimeTypes[469] = "#470 image/x-quicktime (.qtif)";
                mimeTypes[470] = "#471 image/x-rgb (.rgb)";
                mimeTypes[471] = "#472 image/x-tiff (.tif)";
                mimeTypes[472] = "#473 image/x-tiff (.tiff)";
                mimeTypes[473] = "#474 image/x-windows-bmp (.bmp)";
                mimeTypes[474] = "#475 image/x-xbitmap (.xbm)";
                mimeTypes[475] = "#476 image/x-xbm (.xbm)";
                mimeTypes[476] = "#477 image/x-xpixmap (.pm)";
                mimeTypes[477] = "#478 image/x-xpixmap (.xpm)";
                mimeTypes[478] = "#479 image/x-xwd (.xwd)";
                mimeTypes[479] = "#480 image/x-xwindowdump (.xwd)";
                mimeTypes[480] = "#481 image/xbm (.xbm)";
                mimeTypes[481] = "#482 image/xpm (.xpm)";
                mimeTypes[482] = "#483 message/rfc822 (.mht)";
                mimeTypes[483] = "#484 message/rfc822 (.mhtml)";
                mimeTypes[484] = "#485 message/rfc822 (.mime)";
                mimeTypes[485] = "#486 model/iges (.iges)";
                mimeTypes[486] = "#487 model/iges (.igs)";
                mimeTypes[487] = "#488 model/vnd.dwf (.dwf)";
                mimeTypes[488] = "#489 model/vrml (.vrml)";
                mimeTypes[489] = "#490 model/vrml (.wrl)";
                mimeTypes[490] = "#491 model/vrml (.wrz)";
                mimeTypes[491] = "#492 model/x-pov (.pov)";
                mimeTypes[492] = "#493 multipart/x-gzip (.gzip)";
                mimeTypes[493] = "#494 multipart/x-ustar (.ustar)";
                mimeTypes[494] = "#495 multipart/x-zip (.zip)";
                mimeTypes[495] = "#496 music/crescendo (.mid)";
                mimeTypes[496] = "#497 music/crescendo (.midi)";
                mimeTypes[497] = "#498 music/x-karaoke (.kar)";
                mimeTypes[498] = "#499 paleovu/x-pv (.pvu)";
                mimeTypes[499] = "#500 text/asp (.asp)";
                mimeTypes[500] = "#501 text/css (.css)";
                mimeTypes[501] = "#502 text/ecmascript (.js)";
                mimeTypes[502] = "#503 text/html (.acgi)";
                mimeTypes[503] = "#504 text/html (.htm)";
                mimeTypes[504] = "#505 text/html (.html)";
                mimeTypes[505] = "#506 text/html (.htmls)";
                mimeTypes[506] = "#507 text/html (.htx)";
                mimeTypes[507] = "#508 text/html (.shtml)";
                mimeTypes[508] = "#509 text/javascript (.js)";
                mimeTypes[509] = "#510 text/mcf (.mcf)";
                mimeTypes[510] = "#511 text/pascal (.pas)";
                mimeTypes[511] = "#512 text/plain (.c)";
                mimeTypes[512] = "#513 text/plain (.cc)";
                mimeTypes[513] = "#514 text/plain (.com)";
                mimeTypes[514] = "#515 text/plain (.conf)";
                mimeTypes[515] = "#516 text/plain (.c++)";
                mimeTypes[516] = "#517 text/plain (.cxx)";
                mimeTypes[517] = "#518 text/plain (.def)";
                mimeTypes[518] = "#519 text/plain (.f)";
                mimeTypes[519] = "#520 text/plain (.f90)";
                mimeTypes[520] = "#521 text/plain (.for)";
                mimeTypes[521] = "#522 text/plain (.g)";
                mimeTypes[522] = "#523 text/plain (.h)";
                mimeTypes[523] = "#524 text/plain (.hh)";
                mimeTypes[524] = "#525 text/plain (.idc)";
                mimeTypes[525] = "#526 text/plain (.jav)";
                mimeTypes[526] = "#527 text/plain (.java)";
                mimeTypes[527] = "#528 text/plain (.list)";
                mimeTypes[528] = "#529 text/plain (.log)";
                mimeTypes[529] = "#530 text/plain (.lst)";
                mimeTypes[530] = "#531 text/plain (.m)";
                mimeTypes[531] = "#532 text/plain (.mar)";
                mimeTypes[532] = "#533 text/plain (.pl)";
                mimeTypes[533] = "#534 text/plain (.sdml)";
                mimeTypes[534] = "#535 text/plain (.text)";
                mimeTypes[535] = "#536 text/plain (.txt)";
                mimeTypes[536] = "#537 text/richtext (.rt)";
                mimeTypes[537] = "#538 text/richtext (.rtf)";
                mimeTypes[538] = "#539 text/richtext (.rtx)";
                mimeTypes[539] = "#540 text/scriplet (.wsc)";
                mimeTypes[540] = "#541 text/sgml (.sgm)";
                mimeTypes[541] = "#542 text/sgml (.sgml)";
                mimeTypes[542] = "#543 text/tab-separated-values (.tsv)";
                mimeTypes[543] = "#544 text/uri-list (.uni)";
                mimeTypes[544] = "#545 text/uri-list (.unis)";
                mimeTypes[545] = "#546 text/uri-list (.uri)";
                mimeTypes[546] = "#547 text/uri-list (.uris)";
                mimeTypes[547] = "#548 text/vnd.abc (.abc)";
                mimeTypes[548] = "#549 text/vnd.fmi.flexstor (.flx)";
                mimeTypes[549] = "#550 text/vnd.rn-realtext (.rt)";
                mimeTypes[550] = "#551 text/vnd.wap.wml (.wml)";
                mimeTypes[551] = "#552 text/vnd.wap.wmlscript (.wmls)";
                mimeTypes[552] = "#553 text/webviewhtml (.htt)";
                mimeTypes[553] = "#554 text/x-asm (.asm)";
                mimeTypes[554] = "#555 text/x-asm (.s)";
                mimeTypes[555] = "#556 text/x-audiosoft-intra (.aip)";
                mimeTypes[556] = "#557 text/x-c (.c)";
                mimeTypes[557] = "#558 text/x-c (.cc)";
                mimeTypes[558] = "#559 text/x-c (.cpp)";
                mimeTypes[559] = "#560 text/x-component (.htc)";
                mimeTypes[560] = "#561 text/x-fortran (.f)";
                mimeTypes[561] = "#562 text/x-fortran (.f77)";
                mimeTypes[562] = "#563 text/x-fortran (.f90)";
                mimeTypes[563] = "#564 text/x-fortran (.for)";
                mimeTypes[564] = "#565 text/x-h (.h)";
                mimeTypes[565] = "#566 text/x-h (.hh)";
                mimeTypes[566] = "#567 text/x-java-source (.jav)";
                mimeTypes[567] = "#568 text/x-java-source (.java)";
                mimeTypes[568] = "#569 text/x-la-asf (.lsx)";
                mimeTypes[569] = "#570 text/x-m (.m)";
                mimeTypes[570] = "#571 text/x-pascal (.p)";
                mimeTypes[571] = "#572 text/x-script (.hlb)";
                mimeTypes[572] = "#573 text/x-script.csh (.csh)";
                mimeTypes[573] = "#574 text/x-script.elisp (.el)";
                mimeTypes[574] = "#575 text/x-script.guile (.scm)";
                mimeTypes[575] = "#576 text/x-script.ksh (.ksh)";
                mimeTypes[576] = "#577 text/x-script.lisp (.lsp)";
                mimeTypes[577] = "#578 text/x-script.perl (.pl)";
                mimeTypes[578] = "#579 text/x-script.perl-module (.pm)";
                mimeTypes[579] = "#580 text/x-script.phyton (.py)";
                mimeTypes[580] = "#581 text/x-script.rexx (.rexx)";
                mimeTypes[581] = "#582 text/x-script.scheme (.scm)";
                mimeTypes[582] = "#583 text/x-script.sh (.sh)";
                mimeTypes[583] = "#584 text/x-script.tcl (.tcl)";
                mimeTypes[584] = "#585 text/x-script.tcsh (.tcsh)";
                mimeTypes[585] = "#586 text/x-script.zsh (.zsh)";
                mimeTypes[586] = "#587 text/x-server-parsed-html (.shtml)";
                mimeTypes[587] = "#588 text/x-server-parsed-html (.ssi)";
                mimeTypes[588] = "#589 text/x-setext (.etx)";
                mimeTypes[589] = "#590 text/x-sgml (.sgm)";
                mimeTypes[590] = "#591 text/x-sgml (.sgml)";
                mimeTypes[591] = "#592 text/x-speech (.spc)";
                mimeTypes[592] = "#593 text/x-speech (.talk)";
                mimeTypes[593] = "#594 text/x-uil (.uil)";
                mimeTypes[594] = "#595 text/x-uuencode (.uu)";
                mimeTypes[595] = "#596 text/x-uuencode (.uue)";
                mimeTypes[596] = "#597 text/x-vcalendar (.vcs)";
                mimeTypes[597] = "#598 text/x-vcard (.vcf)";
                mimeTypes[598] = "#599 text/xml (.xml)";
                mimeTypes[599] = "#600 video/3gpp (.3gp)";
                mimeTypes[600] = "#601 video/animaflex (.afl)";
                mimeTypes[601] = "#602 video/avi (.avi)";
                mimeTypes[602] = "#603 video/avs-video (.avs)";
                mimeTypes[603] = "#604 video/dl (.dl)";
                mimeTypes[604] = "#605 video/fli (.fli)";
                mimeTypes[605] = "#606 video/gl (.gl)";
                mimeTypes[606] = "#607 video/mp4 (.mp4)";
                mimeTypes[607] = "#608 video/mpeg (.m1v)";
                mimeTypes[608] = "#609 video/mpeg (.m2v)";
                mimeTypes[609] = "#610 video/mpeg (.mp2)";
                mimeTypes[610] = "#611 video/mpeg (.mp4)";
                mimeTypes[611] = "#612 video/mpeg (.mpa)";
                mimeTypes[612] = "#613 video/mpeg (.mpe)";
                mimeTypes[613] = "#614 video/mpeg (.mpeg)";
                mimeTypes[614] = "#615 video/mpeg (.mpg)";
                mimeTypes[615] = "#616 video/msvideo (.avi)";
                mimeTypes[616] = "#617 video/quicktime (.moov)";
                mimeTypes[617] = "#618 video/quicktime (.mov)";
                mimeTypes[618] = "#619 video/quicktime (.qt)";
                mimeTypes[619] = "#620 video/vdo (.vdo)";
                mimeTypes[620] = "#621 video/vivo (.viv)";
                mimeTypes[621] = "#622 video/vivo (.vivo)";
                mimeTypes[622] = "#623 video/vnd.rn-realvideo (.rv)";
                mimeTypes[623] = "#624 video/vnd.vivo (.viv)";
                mimeTypes[624] = "#625 video/vnd.vivo (.vivo)";
                mimeTypes[625] = "#626 video/vosaic (.vos)";
                mimeTypes[626] = "#627 video/x-amt-demorun (.xdr)";
                mimeTypes[627] = "#628 video/x-amt-showrun (.xsr)";
                mimeTypes[628] = "#629 video/x-atomic3d-feature (.fmf)";
                mimeTypes[629] = "#630 video/x-dl (.dl)";
                mimeTypes[630] = "#631 video/x-flv (.flv)";
                mimeTypes[631] = "#632 video/x-gl (.gl)";
                mimeTypes[632] = "#633 video/x-isvideo (.isu)";
                mimeTypes[633] = "#634 video/x-motion-jpeg (.mjpg)";
                mimeTypes[634] = "#635 video/x-mpeg (.mp2)";
                mimeTypes[635] = "#636 video/x-mpeg (.mp3)";
                mimeTypes[636] = "#637 video/x-mpeq2a (.mp2)";
                mimeTypes[637] = "#638 video/x-ms-asf (.asf)";
                mimeTypes[638] = "#639 video/x-ms-asf (.asx)";
                mimeTypes[639] = "#640 video/x-ms-asf-plugin (.asx)";
                mimeTypes[640] = "#641 video/x-ms-wmv (.wmv)";
                mimeTypes[641] = "#642 video/x-msvideo (.avi)";
                mimeTypes[642] = "#643 video/x-qtc (.qtc)";
                mimeTypes[643] = "#644 video/x-scm (.scm)";
                mimeTypes[644] = "#645 video/x-sgi-movie (.movie)";
                mimeTypes[645] = "#646 video/x-sgi-movie (.mv)";
                mimeTypes[646] = "#647 windows/metafile (.wmf)";
                mimeTypes[647] = "#648 www/mime (.mime)";
                mimeTypes[648] = "#649 x-conference/x-cooltalk (.ice)";
                mimeTypes[649] = "#650 x-music/x-midi (.mid)";
                mimeTypes[650] = "#651 x-music/x-midi (.midi)";
                mimeTypes[651] = "#652 x-world/x-3dmf (.3dm)";
                mimeTypes[652] = "#653 x-world/x-3dmf (.3dmf)";
                mimeTypes[653] = "#654 x-world/x-3dmf (.qd3)";
                mimeTypes[654] = "#655 x-world/x-3dmf (.qd3d)";
                mimeTypes[655] = "#656 x-world/x-svr (.svr)";
                mimeTypes[656] = "#657 x-world/x-vrml (.vrml)";
                mimeTypes[657] = "#658 x-world/x-vrml (.wrl)";
                mimeTypes[658] = "#659 x-world/x-vrml (.wrz)";
                mimeTypes[659] = "#660 x-world/x-vrt (.vrt)";
                mimeTypes[660] = "#661 xgl/drawing (.xgz)";
                mimeTypes[661] = "#662 xgl/movie (.xmz)";

                for (int i = 0; i < 661; i++) {
                    contentValues.put("type", mimeTypes[i]);
                    db.insert("mimeTypes", null, contentValues);
                }
                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            }
            mCursor.close();
        }

        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new IRecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
//                    Movie movie = movieList.get(position);
// /                   Toast.makeText(getApplicationContext(), movie.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, MimeTypeSuggestionProvider.AUTHORITY, MimeTypeSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            ArrayList<String> mimeTypesList = new ArrayList<>();
            try {
                Cursor cursor = db.rawQuery("SELECT type FROM mimeTypes WHERE type MATCH ?;", new String[]{query});
                while (cursor.moveToNext()) {
                    mimeTypesList.add(cursor.getString(0));
                }
                cursor.close();
            } catch (Exception e) { Log.e("error", e.getMessage()); }
            mimeTypeAdapter = new MimeTypeAdapter(mimeTypesList);
            recyclerView.setAdapter(mimeTypeAdapter);
            mimeTypeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    private static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private IRecyclerClickListener clickListener;

        RecyclerTouchListener(Context context, final RecyclerView recyclerView, final IRecyclerClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
