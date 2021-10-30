/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy;

import controller.AppCon;
import hierarchy.ranges.RangeDouble;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dimak
 */
public class DemographicInfo {
    public static Map<String,Map<String,Integer>> countryDistributionZip = null;
    public static Map<String,Map<RangeDouble,Integer>> countryDistributionAge = new HashMap<String,Map<RangeDouble,Integer>>(){{
                    put("Kosovo",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),319629);
                        put(new RangeDouble(50.0,64.0),253189);
                        put(new RangeDouble(65.0,79.0),122106);
                        put(new RangeDouble(80.0,120.0),32322);
                        put(new RangeDouble(0.0,14.0),438143);
                        put(new RangeDouble(25.0,49.0),632075);
                    }});
                    put("Cyprus",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),112116);
                        put(new RangeDouble(50.0,64.0),156786);
                        put(new RangeDouble(65.0,79.0),109488);
                        put(new RangeDouble(80.0,120.0),32409);
                        put(new RangeDouble(0.0,14.0),141020);
                        put(new RangeDouble(25.0,49.0),324959);
                    }});
                    put("Czechia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),969132);
                        put(new RangeDouble(50.0,64.0),2002163);
                        put(new RangeDouble(65.0,79.0),1650719);
                        put(new RangeDouble(80.0,120.0),436642);
                        put(new RangeDouble(0.0,14.0),1693319);
                        put(new RangeDouble(25.0,49.0),3908477);
                    }});
                    put("Portugal",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),1089322);
                        put(new RangeDouble(50.0,64.0),2147813);
                        put(new RangeDouble(65.0,79.0),1582600);
                        put(new RangeDouble(80.0,120.0),657704);
                        put(new RangeDouble(0.0,14.0),1407897);
                        put(new RangeDouble(25.0,49.0),3381007);
                    }});
                    put("Iceland",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),47837);
                        put(new RangeDouble(50.0,64.0),63902);
                        put(new RangeDouble(65.0,79.0),38199);
                        put(new RangeDouble(80.0,120.0),12495);
                        put(new RangeDouble(0.0,14.0),67829);
                        put(new RangeDouble(25.0,49.0),126732);
                    }});
                    put("Malta",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),54292);
                        put(new RangeDouble(50.0,64.0),89828);
                        put(new RangeDouble(65.0,79.0),71567);
                        put(new RangeDouble(80.0,120.0),20730);
                        put(new RangeDouble(0.0,14.0),67618);
                        put(new RangeDouble(25.0,49.0),190021);
                    }});
                    put("Greece",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),1093910);
                        put(new RangeDouble(50.0,64.0),2177094);
                        put(new RangeDouble(65.0,79.0),1597966);
                        put(new RangeDouble(80.0,120.0),761447);
                        put(new RangeDouble(0.0,14.0),1533618);
                        put(new RangeDouble(25.0,49.0),3549843);
                    }});
                    put("Armenia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),349902);
                        put(new RangeDouble(50.0,64.0),566367);
                        put(new RangeDouble(65.0,79.0),266875);
                        put(new RangeDouble(80.0,120.0),88959);
                        put(new RangeDouble(0.0,14.0),598985);
                        put(new RangeDouble(25.0,49.0),1094185);
                    }});
                    put("Germany",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),8633999);
                        put(new RangeDouble(50.0,64.0),18928381);
                        put(new RangeDouble(65.0,79.0),12535902);
                        put(new RangeDouble(80.0,120.0),5396249);
                        put(new RangeDouble(0.0,14.0),11290613);
                        put(new RangeDouble(25.0,49.0),26234072);
                    }});
                    put("Latvia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),176638);
                        put(new RangeDouble(50.0,64.0),401274);
                        put(new RangeDouble(65.0,79.0),282236);
                        put(new RangeDouble(80.0,120.0),107519);
                        put(new RangeDouble(0.0,14.0),305275);
                        put(new RangeDouble(25.0,49.0),648950);
                    }});
                    put("Netherlands",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),2125707);
                        put(new RangeDouble(50.0,64.0),3611973);
                        put(new RangeDouble(65.0,79.0),2523196);
                        put(new RangeDouble(80.0,120.0),794980);
                        put(new RangeDouble(0.0,14.0),2747864);
                        put(new RangeDouble(25.0,49.0),5495728);
                    }});
                    put("Austria",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),965607);
                        put(new RangeDouble(50.0,64.0),1922355);
                        put(new RangeDouble(65.0,79.0),1222511);
                        put(new RangeDouble(80.0,120.0),442939);
                        put(new RangeDouble(0.0,14.0),1275664);
                        put(new RangeDouble(25.0,49.0),3011984);
                    }});
                    put("Sweden",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),1156011);
                        put(new RangeDouble(50.0,64.0),1851664);
                        put(new RangeDouble(65.0,79.0),1514068);
                        put(new RangeDouble(80.0,120.0),521740);
                        put(new RangeDouble(0.0,14.0),1820973);
                        put(new RangeDouble(25.0,49.0),3365731);
                    }});
                    put("Ireland",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),617935);
                        put(new RangeDouble(50.0,64.0),843530);
                        put(new RangeDouble(65.0,79.0),529658);
                        put(new RangeDouble(80.0,120.0),161840);
                        put(new RangeDouble(0.0,14.0),1005370);
                        put(new RangeDouble(25.0,49.0),1745910);
                    }});
                    put("Luxembourg",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),71212);
                        put(new RangeDouble(50.0,64.0),121552);
                        put(new RangeDouble(65.0,79.0),63845);
                        put(new RangeDouble(80.0,120.0),24556);
                        put(new RangeDouble(0.0,14.0),98837);
                        put(new RangeDouble(25.0,49.0),234508);
                    }});
                    put("Andorra",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),8075);
                        put(new RangeDouble(50.0,64.0),17140);
                        put(new RangeDouble(65.0,79.0),7771);
                        put(new RangeDouble(80.0,120.0),2591);
                        put(new RangeDouble(0.0,14.0),10589);
                        put(new RangeDouble(25.0,49.0),30014);
                    }});
                    put("Liechtenstein",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),4184);
                        put(new RangeDouble(50.0,64.0),8904);
                        put(new RangeDouble(65.0,79.0),5412);
                        put(new RangeDouble(80.0,120.0),1459);
                        put(new RangeDouble(0.0,14.0),5642);
                        put(new RangeDouble(25.0,49.0),12780);
                    }});
                    put("Poland",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),3911200);
                        put(new RangeDouble(50.0,64.0),7480644);
                        put(new RangeDouble(65.0,79.0),5050384);
                        put(new RangeDouble(80.0,120.0),1670804);
                        put(new RangeDouble(0.0,14.0),5847814);
                        put(new RangeDouble(25.0,49.0),14049941);
                    }});
                    put("Slovenia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),195606);
                        put(new RangeDouble(50.0,64.0),449477);
                        put(new RangeDouble(65.0,79.0),301732);
                        put(new RangeDouble(80.0,120.0),110289);
                        put(new RangeDouble(0.0,14.0),314218);
                        put(new RangeDouble(25.0,49.0),709590);
                    }});
                    put("Slovakia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),566844);
                        put(new RangeDouble(50.0,64.0),1073733);
                        put(new RangeDouble(65.0,79.0),692204);
                        put(new RangeDouble(80.0,120.0),179864);
                        put(new RangeDouble(0.0,14.0),855717);
                        put(new RangeDouble(25.0,49.0),2076611);
                    }});
                    put("Bulgaria",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),623004);
                        put(new RangeDouble(50.0,64.0),1428008);
                        put(new RangeDouble(65.0,79.0),1155007);
                        put(new RangeDouble(80.0,120.0),336002);
                        put(new RangeDouble(0.0,14.0),1008006);
                        put(new RangeDouble(25.0,49.0),2450014);
                    }});
                    put("France",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),7926962);
                        put(new RangeDouble(50.0,64.0),12898107);
                        put(new RangeDouble(65.0,79.0),9337692);
                        put(new RangeDouble(80.0,120.0),4097836);
                        put(new RangeDouble(0.0,14.0),12091975);
                        put(new RangeDouble(25.0,49.0),20825068);
                    }});
                    put("Lithuania",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),293390);
                        put(new RangeDouble(50.0,64.0),611927);
                        put(new RangeDouble(65.0,79.0),391186);
                        put(new RangeDouble(80.0,120.0),162063);
                        put(new RangeDouble(0.0,14.0),421922);
                        put(new RangeDouble(25.0,49.0),913699);
                    }});
                    put("Serbia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),731196);
                        put(new RangeDouble(50.0,64.0),1455427);
                        put(new RangeDouble(65.0,79.0),1100275);
                        put(new RangeDouble(80.0,120.0),320334);
                        put(new RangeDouble(0.0,14.0),995819);
                        put(new RangeDouble(25.0,49.0),2353753);
                    }});
                    put("Romania",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),2057933);
                        put(new RangeDouble(50.0,64.0),3824649);
                        put(new RangeDouble(65.0,79.0),2698610);
                        put(new RangeDouble(80.0,120.0),912480);
                        put(new RangeDouble(0.0,14.0),3048070);
                        put(new RangeDouble(25.0,49.0),6911548);
                    }});
                    put("Hungary",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),1045685);
                        put(new RangeDouble(50.0,64.0),1915461);
                        put(new RangeDouble(65.0,79.0),1456141);
                        put(new RangeDouble(80.0,120.0),430002);
                        put(new RangeDouble(0.0,14.0),1417050);
                        put(new RangeDouble(25.0,49.0),3498647);
                    }});
                    put("Ukraine",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),4030423);
                        put(new RangeDouble(50.0,64.0),8648615);
                        put(new RangeDouble(65.0,79.0),5247946);
                        put(new RangeDouble(80.0,120.0),1805294);
                        put(new RangeDouble(0.0,14.0),6465469);
                        put(new RangeDouble(25.0,49.0),15785821);
                    }});
                    put("United Kingdom",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),7864360);
                        put(new RangeDouble(50.0,64.0),12729599);
                        put(new RangeDouble(65.0,79.0),8930714);
                        put(new RangeDouble(80.0,120.0),3332356);
                        put(new RangeDouble(0.0,14.0),11929834);
                        put(new RangeDouble(25.0,49.0),21860253);
                    }});
                    put("Belarus",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),933308);
                        put(new RangeDouble(50.0,64.0),2008026);
                        put(new RangeDouble(65.0,79.0),1065291);
                        put(new RangeDouble(80.0,120.0),367667);
                        put(new RangeDouble(0.0,14.0),1593222);
                        put(new RangeDouble(25.0,49.0),3450410);
                    }});
                    put("Switzerland",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),905720);
                        put(new RangeDouble(50.0,64.0),1785807);
                        put(new RangeDouble(65.0,79.0),1136423);
                        put(new RangeDouble(80.0,120.0),444316);
                        put(new RangeDouble(0.0,14.0),1281680);
                        put(new RangeDouble(25.0,49.0),2990585);
                    }});
                    put("Spain",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),4599832);
                        put(new RangeDouble(50.0,64.0),9715972);
                        put(new RangeDouble(65.0,79.0),6242629);
                        put(new RangeDouble(80.0,120.0),2863161);
                        put(new RangeDouble(0.0,14.0),6946685);
                        put(new RangeDouble(25.0,49.0),16568783);
                    }});
                    put("Albania",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),443677);
                        put(new RangeDouble(50.0,64.0),578211);
                        put(new RangeDouble(65.0,79.0),326317);
                        put(new RangeDouble(80.0,120.0),77286);
                        put(new RangeDouble(0.0,14.0),492338);
                        put(new RangeDouble(25.0,49.0),944601);
                    }});
                    put("Azerbaijan",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),1407386);
                        put(new RangeDouble(50.0,64.0),1756737);
                        put(new RangeDouble(65.0,79.0),529018);
                        put(new RangeDouble(80.0,120.0),149722);
                        put(new RangeDouble(0.0,14.0),2235847);
                        put(new RangeDouble(25.0,49.0),3902750);
                    }});
                    put("Turkey",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),12956614);
                        put(new RangeDouble(50.0,64.0),12300583);
                        put(new RangeDouble(65.0,79.0),5740272);
                        put(new RangeDouble(80.0,120.0),1476070);
                        put(new RangeDouble(0.0,14.0),19188909);
                        put(new RangeDouble(25.0,49.0),30341437);
                    }});
                    put("Belgium",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),1305930);
                        put(new RangeDouble(50.0,64.0),2302560);
                        put(new RangeDouble(65.0,79.0),1523585);
                        put(new RangeDouble(80.0,120.0),641510);
                        put(new RangeDouble(0.0,14.0),1935983);
                        put(new RangeDouble(25.0,49.0),3745955);
                    }});
                    put("Norway",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),660699);
                        put(new RangeDouble(50.0,64.0),996376);
                        put(new RangeDouble(65.0,79.0),692668);
                        put(new RangeDouble(80.0,120.0),223785);
                        put(new RangeDouble(0.0,14.0),932438);
                        put(new RangeDouble(25.0,49.0),1816921);
                    }});
                    put("Finland",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),618007);
                        put(new RangeDouble(50.0,64.0),1098066);
                        put(new RangeDouble(65.0,79.0),899421);
                        put(new RangeDouble(80.0,120.0),303486);
                        put(new RangeDouble(0.0,14.0),882868);
                        put(new RangeDouble(25.0,49.0),1721591);
                    }});
                    put("Denmark",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),731567);
                        put(new RangeDouble(50.0,64.0),1137992);
                        put(new RangeDouble(65.0,79.0),870913);
                        put(new RangeDouble(80.0,120.0),261274);
                        put(new RangeDouble(0.0,14.0),958004);
                        put(new RangeDouble(25.0,49.0),1840528);
                    }});
                    put("North Macedonia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),257565);
                        put(new RangeDouble(50.0,64.0),409196);
                        put(new RangeDouble(65.0,79.0),238871);
                        put(new RangeDouble(80.0,120.0),51929);
                        put(new RangeDouble(0.0,14.0),340650);
                        put(new RangeDouble(25.0,49.0),778925);
                    }});
                    put("Italy",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),5862034);
                        put(new RangeDouble(50.0,64.0),13099852);
                        put(new RangeDouble(65.0,79.0),9391218);
                        put(new RangeDouble(80.0,120.0),4306801);
                        put(new RangeDouble(0.0,14.0),7895801);
                        put(new RangeDouble(25.0,49.0),19320786);
                    }});
                    put("Georgia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),428199);
                        put(new RangeDouble(50.0,64.0),726076);
                        put(new RangeDouble(65.0,79.0),424475);
                        put(new RangeDouble(80.0,120.0),130322);
                        put(new RangeDouble(0.0,14.0),755864);
                        put(new RangeDouble(25.0,49.0),1262255);
                    }});
                    put("Montenegro",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),80884);
                        put(new RangeDouble(50.0,64.0),123193);
                        put(new RangeDouble(65.0,79.0),74040);
                        put(new RangeDouble(80.0,120.0),20533);
                        put(new RangeDouble(0.0,14.0),111993);
                        put(new RangeDouble(25.0,49.0),212787);
                    }});
                    put("Estonia",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(15.0,24.0),125858);
                        put(new RangeDouble(50.0,64.0),257016);
                        put(new RangeDouble(65.0,79.0),186800);
                        put(new RangeDouble(80.0,120.0),74190);
                        put(new RangeDouble(0.0,14.0),217271);
                        put(new RangeDouble(25.0,49.0),462363);
                    }});
                    put("USA",new HashMap<RangeDouble,Integer>(){{
                        put(new RangeDouble(0.0,5.0),19576683);
                        put(new RangeDouble(5.0,9.0),20195895);
                        put(new RangeDouble(10.0,14.0),20798268);
                        put(new RangeDouble(15.0,19.0),21054570);
                        put(new RangeDouble(20.0,24.0),21632940);
                        put(new RangeDouble(25.0,29.0),23509016);
                        put(new RangeDouble(30.0,34.0),22431305);
                        put(new RangeDouble(35.0,39.0),21737521);
                        put(new RangeDouble(40.0,44.0),19921623);
                        put(new RangeDouble(45.0,49.0),20397751);
                        put(new RangeDouble(50.0,54.0),20477151);
                        put(new RangeDouble(55.0,59.0),21877391);
                        put(new RangeDouble(60.0,64.0),20571146);
                        put(new RangeDouble(65.0,69.0),17455001);
                        put(new RangeDouble(70.0,74.0),14028432);
                        put(new RangeDouble(75.0,79.0),9652665);
                        put(new RangeDouble(80.0,84.0),6317207);
                        put(new RangeDouble(85.0,120.0),6604958);
                    }});
                    }};;

    public static void ReadObject() throws IOException, ClassNotFoundException, URISyntaxException{
        
        if(countryDistributionZip==null){
            String fileName;
            if(AppCon.os.equals("online")){
                fileName = AppCon.rootPath + File.separator + "amnesia" + File.separator+ "ObjectFiles" + File.separator + "countryDistributionZip.ser";
            }
            else{
                fileName = AppCon.parentDir + File.separator + "countryDistributionZip.ser";
            }
            FileInputStream fin = new FileInputStream(fileName);
            BufferedInputStream bf = new BufferedInputStream(fin);
            ObjectInputStream ois = new ObjectInputStream(bf);

            countryDistributionZip = (Map<String,Map<String,Integer>>) ois.readObject();
            System.out.println("Read file!");
            ois.close();
            bf.close();
            fin.close();
        }
    }
}
