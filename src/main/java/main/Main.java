package main;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.UUID;
import java.util.function.Supplier;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;

import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineModeDesc;
import javax.speech.recognition.*;

public class Main extends ResultAdapter {
    private static String ekj;
    public static String ejk;
    public static String ejj;
    public static File datasetFile = new File("src/main/resources/knowledge.csv");
    public static File query = new File("src/main/resources/file.wav");
    public static TrainSet ts = new TrainSet(datasetFile);
    public static HashMap<String, String> dataset = ts.getKnowledge();
    private static final boolean TRACE_MODE = true;
    private static long counter = 0L;
    private static final String smbls = "ABCD37FH.F927RHFNV.WNZ83GGJ1038GNZV";
    private static final File uuid = new File("src/main/resources/uuid.uuid");
    private static Recognizer rec;
    protected static File audio;

    public void resultAccepted(ResultEvent e) {
        Result r = (Result) (e.getSource());
        ResultToken[] tokens = r.getBestTokens();

        for (int i = 0; i < tokens.length; i++)
            System.out.print(tokens[i].getSpokenText() + " ");
        System.out.println();

        try {
            rec.deallocate();
        } catch (EngineException engineException) {
            engineException.printStackTrace();
        }
    }

    public static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm') c += 13;
            else if (c >= 'A' && c <= 'M') c += 13;
            else if (c >= 'n' && c <= 'z') c -= 13;
            else if (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }

    public static String decrypt(String input, Supplier<StringBuilder> supplier) {
        StringBuilder sb = supplier.get();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm') c += 13;
            else if (c >= 'A' && c <= 'M') c += 13;
            else if (c >= 'n' && c <= 'z') c -= 13;
            else if (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getResourcesPath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        return path + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    }

    public static String genUUID() {
        return UUID.randomUUID().toString();
    }

    public static String readFromFile(File myObj, String s) {
        try {
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                s = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return s;

    }

    public static void WriteToFile(String toWrite, File file) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert writer != null;
            writer.write(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static CharSequence getResponse(boolean running, String ms) {
        try {

            String resourcesPath = getResourcesPath();
            MagicBooleans.trace_mode = TRACE_MODE;
            Bot bot = new Bot("emerald", resourcesPath);
            Chat chatSession = new Chat(bot);
            bot.brain.nodeStats();
            String textLine;

            if (running) {
                while (true) {
                    textLine = ms;
                    if ((textLine == null) || (textLine.length() < 1))
                        textLine = MagicStrings.null_input;
                    else {
                        String response = chatSession.multisentenceRespond(textLine);
                        String[] responseMapped = response.split(" ");
                        int counter = 0;

                        for (String mapped : responseMapped) {
                            if (mapped.contains("unknown")) {
                                responseMapped[counter] = "internal processing error";
                                break;
                            }
                            counter++;
                        }

                        while (response.contains("&lt;"))
                            response = response.replace("&lt;", "<");
                        while (response.contains("&gt;"))
                            response = response.replace("&gt;", ">");
                        return response;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }


    public static void run() {
        String s;
        Scanner sc = new Scanner(System.in);
        while (true) {
            s = sc.nextLine();
            tts(answer(s));
            counter += 1;
        }
    }


    public static String listen(File file) throws IOException {
        String s = "";

        try {
            // Create a recognizer that supports English.
            rec = Central.createRecognizer(
                    new EngineModeDesc(Locale.ENGLISH));

            // Start up the recognizer
            rec.allocate();

            // Load the grammar from a file, and enable it
            FileReader reader = new FileReader(file);
            RuleGrammar gram = rec.loadJSGF(reader);
            gram.setEnabled(true);

            // Add the listener to get results
            rec.addResultListener(new Main());

            // Commit the grammar
            rec.commitChanges();

            // Request focus and start listening
            rec.requestFocus();
            rec.resume();
            rec.deallocate();
            s = rec.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }


    public static void learnf(String key, String response) {
        try {
            FileWriter fw = new FileWriter(datasetFile, true);

            fw.write(key);
            fw.write(",");
            fw.write(" ");
            fw.write("\n" + response);

            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String answer(String question) {
        try {
            return (String) getResponse(true, question);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String answerFromSet(String question) {
        ArrayList<String> ary = new ArrayList<>();
        Set<String> keys = dataset.keySet();
        int counter = 0;
        for (String key : keys) {
            String lowerKey = key.toLowerCase();
            String lowerQuestion = question.toLowerCase();
            if (lowerKey.contains(lowerQuestion)) {
                ary.add(key);
            }
        }
        int idx = TrainSet.rndInt(ary);
        if (ary.get(idx) != null) {
            return ary.get(idx);
        } else {
            return null;
        }
    }

    public static String noAnswer(String s) {

        tts("I don't currently understand your query, how would you like me to respond in the future?");

        Scanner sc = new Scanner(System.in);

        String newResp = sc.nextLine();

        learnf(s, newResp);
        return newResp;

    }

    public static void tts(String ToSpeak) {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
            try {
                voice.setRate(130);
                voice.setPitch(150);
                voice.setVolume(3);
                voice.speak(ToSpeak);
                System.out.println(ToSpeak);

            } catch (Exception e1) {
                e1.printStackTrace();
            }

        } else {
            throw new IllegalStateException("Cannot find voice: kevin16");
        }
    }

    public static void main(String[] args) {
        try {
            TrippleDes td = new TrippleDes();

            String encryptedToken = td.encrypt(readFromFile(uuid, ejj));
            System.out.println(encryptedToken);
            if (td.decrypt(encryptedToken).equals(readFromFile(uuid, ejk))) {

                tts("Emerald system online: please allow time for configuration");
                try {
                    run();
                    while (true) {
                        if (counter == 0) {
                            tts("Emerald engine starting, response loading...");
                        }
                        break;
                    }
                } catch (Exception e) {
                    final String message = e.getMessage();
                }
            } else {
                tts("Invalid uuid structure, please try again");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}