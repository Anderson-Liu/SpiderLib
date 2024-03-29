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
 * 读取源数据
 */
public class LoadData {
    public static Map<String, String> getStuList() {

        HashMap<String, String> map = new HashMap<>();
        ArrayList stuList = new ArrayList();
        try {
            String line;
            String pathname = "数据//2013级全体学生.txt";
            System.out.println("读取的文件是《" + pathname + "》");
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


