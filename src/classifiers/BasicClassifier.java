package classifiers;

import java.io.*;
import java.lang.Math;
import java.util.*;
import java.nio.file.Paths;

public class BasicClassifier implements Classifier
{
    private ArrayList<double[]> layers;
    private ArrayList<double[][]> weighs;
    private double[] standardDeviations;
    private double[] means;
    private double[] output;

    /**
     * Alustaa luokittelijan
     * @param nodesPerLayer luokittelijan rakenne (taulukoina solujen määrä kerroksittain)
     */
    public BasicClassifier(int[] nodesPerLayer) throws IOException
    {
        output = new double[nodesPerLayer[0]];
        layers = new ArrayList<double[]>();
        weighs = new ArrayList<double[][]>();

        means = new double[nodesPerLayer[0]];
        standardDeviations = new double[nodesPerLayer[0]];

        for(int i : nodesPerLayer)
            layers.add(new double[i]);

        Scanner sc1;
        int i;
        String s;

        boolean learnedAlready = false;

        while(true)
        {
            // katsotaan, ovatko kaikki CSV:t tallessa, ja saadaanko arvot tuotua onnistuneesti ohjelmaan
            try
            {
                sc1 = new Scanner(new File("mean.csv"));
                sc1.useDelimiter(",");
                i = 0;
                while (sc1.hasNext())
                {
                    s = sc1.next();
                    means[i++] = Double.parseDouble(s);
                }
                sc1.close();

                sc1 = new Scanner(new File("std.csv"));
                sc1.useDelimiter(",");
                i = 0;
                while (sc1.hasNext())
                {
                    s = sc1.next();
                    standardDeviations[i++] = Double.parseDouble(s);
                }
                sc1.close();

                // haetaan hakemistossa sijaitsevat theta#.csv-tiedostot
                File dir = new File(Paths.get("").toAbsolutePath().toString());
                ArrayList<File> filesInDir = new ArrayList<File>();

                for (File f : dir.listFiles())
                {
                    if (f.toString().contains("theta"))
                        filesInDir.add(f);
                }
                Collections.sort(filesInDir);

                int n = 0;
                // jokaista theta-tiedostoa kohtaan luodaan yksi painotusmatriisi
                for (File f : filesInDir)
                {
                    double[][] d = new double[nodesPerLayer[n+1]]
                            [nodesPerLayer[n]+1];
                    sc1 = new Scanner(f);
                    sc1.useDelimiter(",|\\n");
                    for (int j = 0; j < nodesPerLayer[n+1]; j++)
                    {
                        for (int k = 0; k < nodesPerLayer[n]+1; k++)
                        {
                            s = sc1.next();
                            d[j][k] = Double.parseDouble(s);
                        }
                    }
                    weighs.add(d);
                }

                sc1.close();
                break;
            }

            // Mikä tahansa poikkeus -> käynnistetään uudelleenoppiminen (kyllä, huono käytäntö)
            catch (Exception e)
            {
                System.out.println("Cant' read CSVs. Reinitialize learning.");
                try
                {
                    sc1 = new Scanner(new File("octave.txt"));
                    String octavePath = sc1.next();
                    // jos oppimista jo yritetty kertaalleen, se tuskin onnistuu nytkään
                    if(learnedAlready)
                        throw new IOException();
                    learnedAlready = true;

                    makeOctaveCSV();
                    // käynnistetään Octave ja seurataan prosessi läpi
                    Process p = new ProcessBuilder(octavePath, "learning.ex").start();
                    InputStream is = p.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;

                    while ((line = br.readLine()) != null)
                        System.out.println(line);

                }
                catch (IOException ioe)
                {
                    System.out.println("Error in learning: " + ioe);
                    throw ioe;
                }
            }
        }
    }

    /**
     * Tekee arvion kuvassa esiintyvästä hahmosta
     * @param imageFile arvioitava kuvatiedosto (jos null, kuva arvotaan testset-hakemistosta)
     * @return merkkijonotaulukko: arvioitu todennäköisyys, arvioitu hahmo
     */
    public String[] predictImage(File imageFile)
    {
        ImageFeatures imf;
        try
        {
            if(imageFile == null)
            {
                File imageDir = new File("testset");
                File[] filesInDir = imageDir.listFiles();
                Random r = new Random();
                int filenum = r.nextInt(filesInDir.length);
                imageFile = filesInDir[filenum];
                // Debuggaukseen:
                System.out.println(imageFile);
                imf = new ImageFeatures(imageFile);
            }
            else
                imf = new ImageFeatures(imageFile);
        }
        catch(IOException e)
        {
            System.out.println(e);
            return null;
        }

        double[] res = predict(new double[]{imf.RRED, imf.RGREEN, imf.RBLUE,
                imf.RYELLOW, imf.RBROWN, imf.RPURPLE});

        String predicted = "N/A";
        // ennustetun tuloksen mukaan valitaan tulosmerkkijonoon kirjoitettava hahmo
        switch((int)res[0])
        {
            case 1: predicted = "MARIO ; " + res[1]; break;
            case 2: predicted = "LUIGI ; " + res[1]; break;
            case 3: predicted = "TOAD ; " + res[1]; break;
            case 4: predicted = "BOWSER ; " + res[1]; break;
            case 5: predicted = "GOOMBA ; " + res[1]; break;
            case 6: predicted = "WARIO ; " + res[1]; break;
        }

        String[] retVals = predicted.split(";");
        return retVals;
    }

    /**
     * Kuva-arvioinnin käyttöliittymäfunktio
     * @param input taulukko, jossa syötekerroksen mukainen määrä piirteitä
     * @return kaksiulotteinen double-taulukko: 1. hahmon arvioitu luokka, 2: arvioitu todennäköisyys
     */
    private double[] predict(double input[])
    {
        forwardPropagate(input);

        double[] prediction = new double[2];

        double largest = .0;

        // käydään lopputuloskerroksen solmut läpi, valitaan todennäköisyyksistä suurin
        for(int i = 0; i < output.length; i++)
        {
            if(output[i] > largest)
            {
                largest = output[i];
                prediction[0] = i+1;
                prediction[1] = Math.floor(100 * output[i])/100;
            }
        }

        return prediction;
    }

    /**
     * Laskee solun aktivointiarvon
     * @param layerNum solun kerros
     * @param nodeNum solun numero kerroksessa
     */
    private void countNode(int layerNum,  int nodeNum)
    {
        double weightedSum = .0;
        double[] curLayer;
        double[] prevNodes;
        double[] curWeighs;

        try
        {
            curLayer = layers.get(layerNum);
            prevNodes = layers.get(layerNum-1);
            curWeighs = weighs.get(layerNum-1)[nodeNum];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Node error");
            return;
        }

        // lisätään bias
        weightedSum += curWeighs[0];

        // ja lasketaan muut arvot: edellisen kerroksen tietty solmu * sitä vastaava kerroin
        for(int i = 1; i <= prevNodes.length; i++)
            weightedSum += curWeighs[i] * prevNodes[i-1];

        // lasketaan solulle lopullinen aktivointiarvo
        curLayer[nodeNum] = sigmoid(weightedSum);

        // asetetaan kerros takaisin paikalleen, kun solun arvo on laskettu
        // kehno suunnittelu, kehno toteutus, vaatisi kokonaan uuden suunnittelu
        layers.set(layerNum, curLayer);
    }

    /**
     * Prosessoi syötteen neuroverkon läpi
     * @param input taulukko, jossa syötekerroksen mukainen määrä piirteitä
     */
    private void forwardPropagate(double[] input)
    {
        // normalisoidaan syötekerroksen jokainen alkio opetusjoukon parametreilla,
        // koska jokaista uutta syötettä käsitellään yksittäisenä lisäyksenä --> parametrit eivät muutu
        for(int i = 0; i < input.length; i++)
            input[i] = (input[i] - means[i]) / standardDeviations[i];

        double[] layer = layers.get(0);
        try
        {
            System.arraycopy(input, 0, layer, 0, input.length);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Input node error");
            throw e;
        }
        // asetetaan normalisoidut parametrit syötekerrokseen
        layers.set(0, layer);

        // lasketaan aktivointi jokaiselle syötekerroksen jälkeiselle solulle verkossa
        for(int i = 1; i < layers.size(); i++)
            for(int j = 0; j < layers.get(i).length; j++)
                countNode(i, j);

        // tallennetaan lopputuloskerros helpompaa käsittelyä varten
        output = layers.get(layers.size() -1);
    }

    /**
     * Luo oppimiseen käytettävästä kuvajoukosta CSV-tiedoston Octavea varten
     */
    private static void makeOctaveCSV()
    {
        ArrayList<ImageFeatures> features = new ArrayList<ImageFeatures>();

        File imgDir = new File("dataset");
        File[] filesInDir = imgDir.listFiles();

        // jokaisesta opetushakemiston kuvasta luodaan uusi ImageFeatures-olio
        if(filesInDir != null)
        {
            for(File f : filesInDir)
            {
                if(f.isDirectory())
                    continue;
                try
                {
                    features.add(new ImageFeatures(f));
                }
                catch(IOException e)
                {
                    System.out.println("Can't read file: " + f);
                }
                catch(ArithmeticException e)
                {
                    System.out.println("Zero detected pixels: " + f);
                }
            }
        }
        else
            System.out.println("No files");

        String line;
        PrintWriter pw;
        try
        {
            pw = new PrintWriter(new File("dataset.csv"));
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Dataset CSV creation failed.");
            return;
        }

        // tallennetaan CSV-tiedostoon kuvan hahmoluokka ja piirteet
        for(ImageFeatures f : features)
        {
            int actualClass = 0;
            switch(f.NAME)
            {
                case "MARIO": actualClass = 1; break;
                case "LUIGI": actualClass = 2; break;
                case "TOAD": actualClass = 3; break;
                case "BOWSER": actualClass = 4; break;
                case "GOOMBA": actualClass = 5; break;
                case "WARIO": actualClass = 6; break;
            }
            line = (actualClass + "," + f.RRED + "," + f.RGREEN + "," + f.RBLUE
                    + "," + f.RYELLOW + "," + f.RBROWN + "," + f.RPURPLE + "\n");
            pw.write(line);
        }
        pw.close();
        System.out.println("Dataset CSV created.");
    }

    /**
     * Logistinen funktio
     * @param z solulle laskettu painotettu arvo
     * @return logistisen funktion tulos (solun aktivointiarvo)
     */
    private static double sigmoid(double z)
    {
        return (1 / (1 + Math.exp(-z)));
    }

    /**
     * Tulostaa verkon parametrit (vianhakuun, vastaavatko CSV-tiedostojen arvoja)
     */
    public void printStatistics()
    {
        System.out.println("Standard deviations:");
        for(double d : standardDeviations)
            System.out.print(d + " ");

        System.out.println("\n\nMeans:");
        for(double d: means)
            System.out.print(d + " ");

        System.out.println("\n\nLayer sizes:");
        for(double[] l : layers)
            System.out.print(l.length + " ");

        System.out.println("\n\nWeighs:");
        for(double[][] d : weighs)
        {
            for(int i = 0; i < d.length; i++)
            {
                for (int j = 0; j < d[i].length; j++)
                {
                    System.out.print(d[i][j] + " ");
                }
                System.out.println("");
            }
            System.out.println("");
        }
    }
}

