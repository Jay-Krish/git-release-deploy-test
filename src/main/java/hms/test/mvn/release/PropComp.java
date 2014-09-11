package hms.test.mvn.release;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PropComp {

    public static boolean result = true;

    private static void walk(String dir, List<File> fileList) {
        try {
            File[] files = (new File((new File(".")).getCanonicalPath() + File.separator + dir)).listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    walk(dir + File.separator + file.getName(), fileList);
                } else {
                    fileList.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, File> getFiles(String dir) throws IOException {


        List<File> oriFiles = new ArrayList<File>();
        walk(dir, oriFiles);

        int initialSize = oriFiles.size();
        //Converting ArrayList to HashSet to remove duplicates
        HashSet<File> listToSet = new HashSet<File>(oriFiles);

        //Creating Arraylist without duplicate values
        List<File> listWithoutDuplicates = new ArrayList<File>(listToSet);
        if (listWithoutDuplicates.size() != initialSize) {
            throw new RuntimeException("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& " + String.valueOf(initialSize - listWithoutDuplicates.size()) + " Duplicate files found in given dir!! : " + dir);
        }

        Map<String, File> propfiles = new HashMap<String, File>();
        for (File file : listWithoutDuplicates) {

            String fileName = file.getName();

            if (!file.isDirectory() &&
                    (fileName.endsWith(".properties") ||
                            (fileName.startsWith("wrapper") && fileName.endsWith(".conf")))) {
                propfiles.put(file.getName(), file);
            }
        }
        return propfiles;
    }

    public static void main(String[] args) throws IOException {

        PropComp fileToMap = new PropComp();

        Map<String, File> oriFiles = getFiles("WorkingModule");
        Map<String, File> cmpFiles = getFiles("NewModule");
        System.out.println("Total files to compare : " + oriFiles.size());

        for (Map.Entry<String, File> oriFile : oriFiles.entrySet()) {
            System.out.println("comparing : " + oriFile.getKey());
            if (cmpFiles.containsKey(oriFile.getKey())) {
                HashMap<String, String> oriMap = fileToMap.loadPropToMap(oriFile.getValue().getPath());
                HashMap<String, String> cmpMap = fileToMap.loadPropToMap(cmpFiles.get(oriFile.getKey()).getPath());
                fileToMap.comparePropMaps(oriMap, cmpMap, oriFile.getValue().getName());
            } else {
                System.out.println("------------------------  PropFile not found !! ---------------------------------");
                System.out.println("\tMissing: " + oriFile.getKey());
                result = false;
            }
        }
        System.out.println("==================================compared files result: " + result);
    }

    private File getPropertiesFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private HashMap<String, String> loadPropToMap(String filePath) {
        HashMap<String, String> map = new HashMap<String, String>();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(getPropertiesFile(filePath)));
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                map.put(key, value);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private boolean comparePropMaps(HashMap<String, String> oriMap, HashMap<String, String> cmpMap, String fileName) {

        for (Map.Entry<String, String> oriEntry : oriMap.entrySet()) {
            if (cmpMap.containsKey(oriEntry.getKey())) {
                if (!cmpMap.get(oriEntry.getKey()).equals(oriEntry.getValue())) {
                    System.out.println("############## Value Mismatch !! ################## ");
                    System.out.println("\tIn file: " + fileName);
                    System.out.println("\tKey: " + oriEntry.getKey() + "\n" +
                            "\t\t Original Value: " + oriEntry.getValue() + "\n" +
                            "\t\t Compare  Value: " + cmpMap.get(oriEntry.getKey()) + "\n");
                    result = false;
                }
            } else {
                System.out.println("------------------------  Key not found !! ----------------------");
                System.out.println("\tIn file: " + fileName);
                System.out.println("\tMissing: " + oriEntry.getKey());
                result = false;
            }
        }
        return result;
    }

}
