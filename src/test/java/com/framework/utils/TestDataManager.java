package com.framework.utils;
/*
 ╔══════════════════════════════════════════════════════════╗
 ║               TEST DATA MANAGER                         ║
 ╠══════════════════════════════════════════════════════════╣
 ║  PURPOSE : Reads JSON test data files — never hardcode  ║
 ║            test data in test classes                    ║
 ║                                                         ║
 ║  PATTERN : Utility class — all methods static           ║
 ║            Private constructor — no instances allowed   ║
 ║            Cache — each file read ONCE per session      ║
 ║                                                         ║
 ║  LOCATION: src/test/resources/testdata/                 ║
 ║                                                         ║
 ║  USAGE   : Map<String,String> data =                    ║
 ║              TestDataManager.getLoginData("validUser")  ║
 ║            String user = data.get("username")           ║
 ║            String pass = data.get("password")           ║
 ╚══════════════════════════════════════════════════════════╝
Jackson reads JSON JsonNode — represents one node in the parsed JSON tree
ObjectMapper — the actual JSON reader/parser
log4j for logging Log manager is factory while logger is type
java reads files
java stores data Hashmap is implemented while Map is interface
*/


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class TestDataManager {
    /*
 ┌─────────────────────────────────────────────────────────┐
 │  FIELDS                                                 │
 │  All static final — created once, shared everywhere     │

*/
    private static final Logger log = LogManager.getLogger(TestDataManager.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, JsonNode> cache = new HashMap<>();

    private TestDataManager() {

    }
    /*
 ╭─────────────────────────────────────────────────────────╮
 │  loadFile()                                             │
 ├─────────────────────────────────────────────────────────┤
 │  WHAT    : Reads JSON file from testdata/ folder        │
 │  IN      : fileName → "android/login_data"              │
 │  OUT     : JsonNode → entire parsed JSON tree           │
 │  CACHE   : Stores result — file read ONCE per session   │
 │  THROWS  : RuntimeException if file not found           │

*/

 /*   public static JsonNode loadFile(String fileName) {

        if (cache.containsKey(fileName)) {
            log.info("cache hit :{}", fileName);
            return cache.get(fileName);

        }
        try {
            String path = "testdata/" + fileName + ".json";
            InputStream is = TestDataManager.class
                    .getClassLoader()
                    .getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("Test Data file not found check Test Data Path" + path);
            }
            JsonNode root = mapper.readTree(is);
            cache.put(fileName, root);
            log.info("Test data loaded: {}", path);
            return root;

        } catch (Exception e) {
            throw new RuntimeException("cannot read Test Data" + fileName, e);

        }


    }
*/
 public static JsonNode loadFile(String fileName) {
     if (cache.containsKey(fileName)) {
         log.info("cache hit: {}", fileName);
         return cache.get(fileName);
     }

     // Format the target string (e.g., "testdata/android/login_data.json")
     String path = "testdata/" + fileName + ".json";

     // 🔴 FIX: Using Thread Context ClassLoader handles environments cleaner
     try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
         if (is == null) {
             throw new RuntimeException("Test Data file not found check Test Data Path: " + path);
         }
         JsonNode root = mapper.readTree(is);
         cache.put(fileName, root);
         log.info("Test data loaded successfully: {}", path);
         return root;
     } catch (Exception e) {
         throw new RuntimeException("cannot read Test Data: " + fileName, e);
     }
 }

    /*
     ╭─────────────────────────────────────────────────────╮
     │  getData()                                          │
     ├─────────────────────────────────────────────────────┤
     │  WHAT  : Get one data set by file name and key      │
     │  IN    : fileName → "android/login_data"            │
     │          key      → "validUser"                     │
     │  OUT   : Map<String,String> → field → value         │
     │                                                     │
     │  USAGE :                                            │
     │  Map<String,String> data =                          │
     │      TestDataManager.getData(                       │
     │          "android/login_data", "validUser");        │
     │  String user = data.get("username");                │
     ╰────────────────────────────────────────────────
    */

    public static Map<String,String> getData(String fileName, String key){
        try {
            JsonNode root = loadFile(fileName);
            JsonNode node = root.get(key);
            if (node == null) {
                throw new RuntimeException("Key" + key + "not found" + fileName + ".json" + " Available Keys :" + root.fieldNames());
            }
                Map<String, String> data = new HashMap<>();
                node.fields().forEachRemaining(entry -> data.put(entry.getKey(), entry.getValue().asText()));
                log.debug("Data Loaded: {} {}", fileName, key);
                return data;
            } catch (Exception e){
                throw new RuntimeException("failed to get data:"+ fileName + "->" + key,e);
            }

    }
    /*
     ╭─────────────────────────────────────────────────────╮
     │  getLoginData()                                     │
     ├─────────────────────────────────────────────────────┤
     │  WHAT  : Shortcut for android login test data       │
     │  IN    : key → "validUser" / "invalidUser" etc      │
     │  OUT   : Map<String,String> → username, password    │
     │                                                     │
     │  USAGE :                                            │
     │  Map<String,String> data =                          │
     │      TestDataManager.getLoginData("validUser");     │
     │  loginPage.login(                                   │
     │      data.get("username"),                          │
     │      data.get("password"));                         │
     ╰─────────────────────────────────────────────────────╯
    */
    public static Map<String, String> getLoginData(String key) {
        // ⚑ RULE: Shortcut method — saves typing file path
        // Instead of: getData("android/login_data", key)
        // Just call:  getLoginData("validUser")
        return getData("android/login_data", key);
    }
    /*
     ╭─────────────────────────────────────────────────────╮
     │  getValue()                                         │
     ├─────────────────────────────────────────────────────┤
     │  WHAT  : Get a single field value directly          │
     │  IN    : fileName, key, field name                  │
     │  OUT   : String → the single value                  │
     │                                                     │
     │  USAGE :                                            │
     │  String username = TestDataManager.getValue(        │
     │      "android/login_data",                          │
     │      "validUser",                                   │
     │      "username");                                   │
     ╰───────
    */

    public static String getValue(String fileName, String field, String key){
        return getData(fileName,key).getOrDefault(field,"");

    }

}
