package core.other;

import java.io.File;

import java.io.*;
import java.util.*;

public class TrainSet {

    private static HashMap<String, String> knowledge;

    public static int rndInt(ArrayList<String> ary) {
        Random r = new Random();

        return r.nextInt(ary.size());
    }

    public TrainSet(File dataset) {

        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(dataset));
            while ((line = br.readLine()) != null) {

                String[] knowledgeAry = line.split(",");
                knowledge = new HashMap<String, String>();

                    for (int i = 0; i < knowledgeAry.length; i++) {

                        for (int j = 1; j < knowledgeAry.length; j++) {

                            knowledgeAry[i] = knowledgeAry[i].toLowerCase(Locale.ROOT);
                            knowledgeAry[j] = knowledgeAry[j].toLowerCase(Locale.ROOT);

                            knowledge.put(knowledgeAry[i], knowledgeAry[j]);

                        }

                    }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public HashMap<String, String> getKnowledge() {

        return knowledge;

    }
}
