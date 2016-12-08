import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Enn on 2016/3/20.
 */
public class LoadData {
    public static Map<String, String> getStuList() {

        HashMap<String, String> map = new HashMap<>();
        ArrayList stuList = new ArrayList();
        try {
            String line;
            String pathname = "数据//14级转专业.txt";
            File file = new File(pathname);
            Scanner scanner = new Scanner(file);
            String[] namePair = {};
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                if (!line.isEmpty()) {
                    namePair = line.split("\t");
                    if (namePair.length >= 2) {
                        map.put(namePair[0], namePair[1]);
                        stuList.add(namePair);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void main(String args[]) {
        getStuList();
    }
}


