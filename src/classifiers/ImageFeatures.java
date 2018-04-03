package classifiers;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

class ImageFeatures
{
    // kuvasta havaittujen pikselien suhteet
    public final double RRED;
    public final double RGREEN;
    public final double RBLUE;
    public final double RYELLOW;
    public final double RBROWN;
    public final double RPURPLE;

    // hahmon nimi
    public final String NAME;

    /**
     * Alustetaan olio
     * @param file kuvatiedosto, josta olio alustetaan
     * @throws IOException
     * @throws ArithmeticException
     */
    ImageFeatures(File file) throws IOException, ArithmeticException
    {
        BufferedImage im;

        try
        {
            im = ImageIO.read(file);
        }
        catch(IOException e)
        {
            im = null;
            throw e;
        }

        // haetaan hahmon nimi tiedostonimestä, jos muotoa luku_nimi (testausta varten, ei pakollinen)
        String nn = "";
        try
        {
            nn = file.toString().split("_")[1].split("\\.")[0];
        }
        catch(Exception e)
        {
            nn = "N/A";
        }
        NAME = nn;

        // luetaan pikselien määrät
        int[] pixelCount = walkPixels(im);

        // the total number of DETECTED pixels in the image
        int total = 0;
        for(int i : pixelCount)
            total += i;

        // no detected colors
        if(total == 0)
            throw new ArithmeticException();

        // asetetaan pikselien suhteet
        RRED = Math.floor(100 * ( (double)pixelCount[0] / total)) / 100;
        RGREEN = Math.floor(100 * ( (double)pixelCount[1] / total)) / 100;
        RBLUE = Math.floor(100 * ( (double)pixelCount[2] / total)) / 100;
        RYELLOW = Math.floor(100 * ( (double)pixelCount[3] / total)) / 100;
        RBROWN = Math.floor(100 * ( (double)pixelCount[4] / total)) / 100;
        RPURPLE = Math.floor(100 * ( (double)pixelCount[5] / total)) / 100;
    }

    /**
     * Käy kuvatiedoston läpi pikselikohtaisesti
     * @param im kuvatiedosto BufferedImage-muodossa
     * @return int-taulukko, jossa värikohtaiset pikselimäärät
     */
    private int[] walkPixels(BufferedImage im)
    {
        int intPixel, red, green, blue;
        int width = im.getWidth();
        int height = im.getHeight();

        int[] pixelCount = new int[6]; //red, green, blue, yellow, brown, purple

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                intPixel = im.getRGB(x, y);
                // RGB-kohtaiset kirkkaudet
                red = (intPixel >> 16) & 0xff;
                green = (intPixel >> 8) & 0xff;
                blue = intPixel & 0xff;

                // approksimoidaan, onko pikseli väriltään suunnilleen jokin etsityistä
                if (red < 225 && red > 150 && red > green + 70 && red > blue + 100) // brown
                    pixelCount[4]++;
                else if (red > 150 && green < 120 && blue < 120) // red
                    pixelCount[0]++;
                else if (green > 150 && (red + blue) < 200) // green
                    pixelCount[1]++;
                else if (blue > 150 && (red + green) < 100) // blue
                    pixelCount[2]++;
                else if (red > 170 && green > 150 && blue < 100) // yellow
                    pixelCount[3]++;
                else if (blue > 100 && red > 100 && green < 80) // purple
                    pixelCount[5]++;
            }
        }
        return pixelCount;
    }
}

