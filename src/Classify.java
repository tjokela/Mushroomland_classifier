/*****************************
 * Main-funktio testejä varten. Voi poistaa.
 */

import classifiers.BasicClassifier;

import java.io.IOException;

public class Classify
{
    public static void main(String[] args)
    {
        String[] sarr = new String[2];
        BasicClassifier bc = null;
        try
        {
            bc = new BasicClassifier(new int[]{6, 6, 6});
        }
        catch(IOException e)
        {
            System.out.println(e);
            return;
        }
        //bc.printStatistics();
        sarr = bc.predictImage(null);
        System.out.println(sarr[0] + "\t\t" + sarr[1]);
        sarr = bc.predictImage(null);
        System.out.println(sarr[0] + "\t\t" + sarr[1]);
        sarr = bc.predictImage(null);
        System.out.println(sarr[0] + "\t\t" + sarr[1]);
        sarr = bc.predictImage(null);
        System.out.println(sarr[0] + "\t\t" + sarr[1]);
        sarr = bc.predictImage(null);
        System.out.println(sarr[0] + "\t\t" + sarr[1]);
    }

}