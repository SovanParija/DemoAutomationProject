package com.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.util.Map;


/**
 * ConfigManager- singleton pattern that loads config.yaml
 * and provides typed getters for every framework settings
 *
 * Usage:
 * ConfigManager config = new ConfigManager.getInstance();
 * string platform = config.getPlatform();
 */


@SuppressWarnings("unchecked")
public class ConfigManager {

    //------Logger----------------------------
    private  static  final Logger log = LogManager.getLogger(ConfigManager.class);
    //-------Holds the entire YAML file as a map
    private Map<String,Object> config;
    //to keep thread synchronization
    private static ConfigManager instance;
    //private constructor-loads YAML on creation
    private ConfigManager(){
        loadConfig();

    }
    //This function is used to put a lock on the door suppose 10 tests starts and invoke
    //the config manager would be created 10 times as all those could see instance == null
    //so synchronized puts a lock at the door only one thread enters
    //Result YAML file is read only once first time instance is null hence forth instance would never
    //be null again so it return same object and it lives for entire test run
    public static synchronized ConfigManager getInstance(){
        if (instance == null){
            instance = new ConfigManager();

        }
        return instance;
    }
   //----------YAML LOADING--------------------

   private void loadConfig(){
        try{
            //create a jackson yaml reader
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            //reads from src/main/resources/config.yaml finds the class path
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.yaml");
            //Crash Early if file missing
            if (is == null){
                throw new RuntimeException("config.yaml is not found in src/main/resources/config.yaml");
            }
            //parse YAML to Java Map
            config = mapper.readValue(is,Map.class);
            //conform Success
            log.info("config.yaml loaded - platform: {}",getPlatform());
        }catch(Exception e) {
            throw new RuntimeException("failed to load config.yaml:" + e.getMessage(), e);
        }
        }


    public String getPlatform() {
        // Check system property first — set by Maven profile
        String sysProp = System.getProperty("platform");
        if (sysProp != null && !sysProp.isEmpty()) {
            return sysProp.toLowerCase().trim();
        }
        // Fall back to config.yaml
        return getString("platform").toLowerCase().trim();
    }

    public boolean isAndroid(){
        return getPlatform().equals("android");

    }

    public boolean isIOS(){
        return getPlatform().equals("ios");

    }

    public boolean isBrowserStack(){
        return getPlatform().equals("browserstack");

    }

    public String  getAppiumHost(){
        return getNested("appium","server","host");

    }
    public int getAppiumPort(){
        return getNestedInt("appium","server","port");

    }
    public int getImplicitTimeout(){
        return getNestedInt("timeouts","implicit");

    }

    public int getExplicitTimeout(){
        return getNestedInt("timeouts","explicit");

    }

    public int getPageLoadTimeout(){
        return getNestedInt("timeouts","pageLoad");

    }

    public String getAndroidDevice(){
        return getNested("android","deviceName");

    }

    public String getAndroidVersion(){
        return getNested("android","platformVersion");

    }

    public String getAndroidAuto(){
        return getNested("android","automationName");

    }

    public String getAppPackage(){
        return getNested("android","appPackage");

    }

    public String getAndroidActivity(){
        return getNested("android","appActivity");

    }
    public String getAndroidAppPath(){
        return getNested("android","appPath");

    }
    public boolean getNoReset(){
        return getNestedBoolean("android","noReset");

    }

    public boolean getAutoGrant(){
        return getNestedBoolean("android","autoGrantPermission");

    }

    public String getIOSDevice(){
        return getNested("ios","deviceName");

    }

    public String getIOSPlatformVersion(){
        return getNested("ios","platformVersion");

    }

    public String getIOSAuto(){
        return getNested("ios","automationName");

    }

    public String getIOSBundleId(){
        return getNested("ios","bundleId");

    }

    public String getIOSAppPath(){
        return getNested("ios","appPath");

    }
    public String getIOSUdid() {
        return getNested("ios","udid");
    }

    public String getWdaLaunchTimeout(){
        return getNested("ios","wdaLaunchTimeout");

    }
    public String getBrowserstackUsername(){
        return getNested("browserstack","username");

    }

    public String getBrowserstackAccessKey(){
        return getNested("browserstack","accessKey");

    }

    public String getBSHub(){
        return getNested("browserstack","hub");

    }

    public Map<String,Object> getBSAndroidConfig(){
        Map<String,Object> bs = (Map<String, Object>) config.get("browserstack");
        return (Map<String, Object>) bs.get("android");
    }

    public Map<String,Object> getBSIOSConfig(){
        Map<String,Object> bs = (Map<String, Object>) config.get("browserstack");
        return (Map<String, Object>) bs.get("ios");
    }

    public String getScreenshotPath(){
        return getNested("screenshots","path");
    }

    public Boolean getScreenshotOnFailure(){
        return getNestedBoolean("screenshots","onFailure");
    }



    private String getNested(String... keys){
        //to start at very top of config map
        Object current = config;
        //Walk through each key one by one
        for (String key :keys) {
            //is current still present in map
            if (current instanceof Map) {
                //go one level deep
                current = ((Map<?, ?>) current).get(key);
            }else{
                //hit an dead end key doesn't exist
                return "";

            }
        }
        //return the final value as string
        //or("" if it came back null"
        return current != null? current.toString() : "";
    }

    private int getNestedInt(String... keys){
        try{
            //get the value as a string first
            //convert string to integer
            //if conversion fails return 0 as safe default
            return Integer.parseInt(getNested(keys));
        }catch (NumberFormatException e){
            return 0;
        }
    }

    private boolean getNestedBoolean(String... keys){
        return Boolean.parseBoolean(getNested(keys));
    }

    private String getString(String key){
        Object val = config.get(key);
        return val !=null ? val.toString() :"";

    }
    // this function looks at a value from config.yaml if that value is a real placeholder ${BS_USERNAME}
    //this function goes and finds the real value from computer environment variables if
    //not placeholder it returns the value as is
private String resolveEnv(String value)
{
    if (value != null && value.startsWith("${") && value.endsWith("}")){
        String key = value.substring(2,value.length()-1);
        String env = System.getenv(key);
        return  env != null ? env : System.getProperty(key,"");
    }
    return value;
}


}
