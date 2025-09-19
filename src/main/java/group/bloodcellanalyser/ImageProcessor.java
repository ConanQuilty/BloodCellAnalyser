package group.bloodcellanalyser;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ImageProcessor {

    private DisjointSetController<Integer> DSC = new DisjointSetController<>();
    private DisjointSetNode<Integer>[] imgSet;
    private Set<DisjointSetNode<Integer>> cellRootSet = new HashSet<>();
    private int blankVal = 0;
    private int redVal = 1;
    private int whiteVal = 2;


    public Image imageRedBlue(Image img){
        System.out.println("Img red and blue running");

        int width = (int) img.getWidth();
        int height = (int) img.getHeight();

        PixelReader pxReader = img.getPixelReader();
        WritableImage newImg = new WritableImage(width,height);
        PixelWriter pxWriter = newImg.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pxReader.getColor(x, y);

                double hue = color.getHue(); // 0-360° (red ~0 or 330-360, purple ~200-270)
                double saturation = color.getSaturation();
                double brightness = color.getBrightness();

                if ((hue >= 310 || hue <= 50) && saturation > 0.15 && brightness > 0.2) {
                    // Red
                    pxWriter.setColor(x, y, Color.RED);
                } else if (hue >= 180 && hue <= 270 && saturation > 0.15 && brightness > 0.2) {
                    // Purple
                    pxWriter.setColor(x, y, Color.PURPLE);
                } else {
                    // Everything else is white
                    pxWriter.setColor(x, y, Color.WHITE);
                }
            }
        }
        return newImg;
    }

    /**
     * Creates a disjoint set for the image, gives each node a value depending on color, then unionises nodes
     * @param baseImage
     */
    public int[] cellsCounter(Image baseImage){
        System.out.println("Cell counter running");

        Image image = imageRedBlue(baseImage);
        int imgWidth = (int) image.getWidth();
        int imgHeight = (int) image.getHeight();
        System.out.println("Image size: X" + imgWidth+ " * Y"+ imgHeight);
        System.out.println("Expected pixel count: "+(imgHeight*imgWidth));

        imgSet = new DisjointSetNode[imgWidth * imgHeight];

        PixelReader pReader = image.getPixelReader();
        int currPixel = 0;

        System.out.println("Creating imgSet");
        //cycle through image, make a set that we can apply find and union to
        for(int y = 0 ; y<imgHeight; y++){
            for(int x = 0; x<imgWidth; x++){
                System.out.println("ImgSet for pixel: X"+x+",Y"+y);
                imgSet[currPixel] = new DisjointSetNode<>(null,x,y);
                Color currColour = pReader.getColor(x, y);
                if (currColour.equals(Color.RED)){
                    imgSet[currPixel].setData(redVal);
                } else if (currColour.equals(Color.PURPLE)) {
                    imgSet[currPixel].setData(whiteVal);
                } else {
                    imgSet[currPixel].setData(blankVal);
                }
                currPixel++;
            }
        }

        System.out.println("Unionising sets");
        //cycle through to union cells
        for(int i =0 ; i < imgSet.length;i++){
            //Ignore anything not a cell
            System.out.println("Working on pixel: "+ i + "CellVal = "+imgSet[i].getData() );
            if(imgSet[i].getData() != blankVal){
                int cellVal = imgSet[i].getData();

                //check to the right of i, union if same type
                System.out.print("Check right->: ");
                if((i % imgWidth) != (imgWidth - 1)
                        && (i+1) < imgSet.length
                        && imgSet[i+1].getData() == cellVal ){
                    System.out.print("Found cell to right |");
                    DSC.unionBySize(imgSet[i],imgSet[i+1]);
                }

                //check below i, by adding width
                System.out.print(" Check down V: ");
                if((i+imgWidth) < imgSet.length && imgSet[i + imgWidth].getData() == cellVal){
                    System.out.print("Found cell down |");
                    DSC.unionBySize(imgSet[i],imgSet[i + imgWidth]);
                }
            }

        }

        System.out.println("counting each");
        cellRootSet.clear();
        //find all the roots of the imgSet and add to a hash set which only has unique elements to find the amount of cells
        for(int i = 0; i<imgSet.length;i++){
            DisjointSetNode<Integer> root = DSC.find(imgSet[i]);
            if (root.getData() != blankVal){
                cellRootSet.add(root);
            }
        }

        System.out.println("Amount of cells: "+ cellRootSet.size());
        int redCells= 0;
        int whiteCells = 0;
        for (DisjointSetNode<Integer> i : cellRootSet){
            if(i.getData() == redVal){
                redCells+=1;
            } else {
                whiteCells+=1;
            }
        }
        int[] data = {redCells,whiteCells};
        System.out.println("Red count: "+redCells+" White count: "+ whiteCells);
        return data;
    }


    /**
     * Removes cells smaller than min, declares cluster if larger than max
     * @param maxSize
     * @param minSize
     */
    public int[] removeNoise(int maxSize, int minSize){
        Set<DisjointSetNode<Integer>> clusterSet = new HashSet<>();
        Set<DisjointSetNode<Integer>> deleteCells = new HashSet<>();
        for(DisjointSetNode<Integer> cell : cellRootSet){
            if (cell.getSize() > maxSize){
                System.out.println(cell+" <- is above max");
                clusterSet.add(cell);
            } else if (cell.getSize() < minSize){
                System.out.println(cell+" <- Falls below min");
                deleteCells.add(cell);
            }
        }

        for (DisjointSetNode<Integer> cellToRemove : deleteCells){
            cellRootSet.remove(cellToRemove);
        }
        System.out.println("The following cells are likely to be clusters");
        System.out.println(clusterSet);


        int redCells= 0;
        int whiteCells = 0;
        for (DisjointSetNode<Integer> i : cellRootSet){
            if(i.getData() == redVal){
                redCells+=1;
            } else {
                whiteCells+=1;
            }
        }
        System.out.println("Red count: "+redCells+" White count: "+ whiteCells);
        int[] data = {redCells,whiteCells};
        return data;
    }

    /**
     * Go through the imgSet and find the data for boxes
     * @return hashmap<cellNumber, int[cellType,topLeftX,topLeftY,width,height]
     */
    public HashMap<Integer, int[]> getBoxInfo(){
        HashMap<Integer, int[]> cellDataMap = new HashMap<>();
        int cellNumber = 0;
        //run through the image set and find the max xy and min xy
        for (DisjointSetNode<Integer> root : cellRootSet){
            cellNumber++;
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (DisjointSetNode<Integer> node : imgSet){
                if (DSC.find(node) == root){
                    int x = node.getX();
                    int y = node.getY();

                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);


                }
            }
            int width = maxX - minX;
            int height = maxY - minY;

            //add the cell type, top left x + y, width , height
            int[] data = {root.getData(),minX,minY,width,height};
            cellDataMap.put(cellNumber,data);
            String dataString="";
            for(int i : data){
                dataString += i + " ";
            }
            System.out.println("Cell number "+cellNumber+" Data "+ dataString);
        }
        return cellDataMap;
    }

    public int numberOfCells(){
        return cellRootSet.size();
    }

}
