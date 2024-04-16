package org.georoot.RoutingEngine;

import com.graphhopper.reader.osm.Pair;
import org.locationtech.proj4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Main {
    public static Logger logger = LoggerFactory.getLogger(Main.class);
    public static GHRoutingEngine engine;
    public static CoordinateTransform toWGS84;
    public static ArrayList<ProjCoordinate> from = new ArrayList<>();
    public static ArrayList<ProjCoordinate> to = new ArrayList<>();
    public static void main(String[] args) {
        handleArgs(args);
        engine = new GHRoutingEngine();
        engine.setOSMFile(args[0]);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("DistanceMatrix.csv")));
            writer.write("id,startLongitude,startLatitude,endLongitude,endLatitude,distance(meters),time(ms)");
            writer.newLine();
            Queue<Pair<ProjCoordinate, ProjCoordinate>> queue = getQueue();
            int id = 0;
            while (!queue.isEmpty()){
                Pair<ProjCoordinate, ProjCoordinate> poll = queue.poll();
                Pair<Double, Long> route = engine.route(poll.first, poll.second);
                String[] out = {String.valueOf(id), String.valueOf(poll.first.x),String.valueOf(poll.first.y), String.valueOf(poll.second.x),String.valueOf(poll.second.y), String.valueOf(route.first), String.valueOf(route.second)};
                writer.write(String.join(",",out));
                writer.newLine();
                id++;
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        engine.close();
    }
    public static Queue<Pair<ProjCoordinate,ProjCoordinate>> getQueue(){
        ArrayList<Pair<ProjCoordinate,ProjCoordinate>> routes = new ArrayList<>();
        for (int i = 0; i < from.size(); i++) {
            for (int j = 0; j < to.size(); j++) {
                routes.add(new Pair<>(from.get(i),to.get(j)));
            }
        }
        return new LinkedList<>(sortByDistance(routes));
    }
    // Method to calculate the Euclidean distance between two ProjCoordinate points
    private static double calculateDistance(ProjCoordinate p1, ProjCoordinate p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Method to sort the ArrayList of Pairs by the distance between the coordinates
    public static ArrayList<Pair<ProjCoordinate, ProjCoordinate>> sortByDistance(ArrayList<Pair<ProjCoordinate, ProjCoordinate>> routes) {
        Collections.sort(routes, new Comparator<Pair<ProjCoordinate, ProjCoordinate>>() {
            @Override
            public int compare(Pair<ProjCoordinate, ProjCoordinate> p1, Pair<ProjCoordinate, ProjCoordinate> p2) {
                double dist1 = calculateDistance(p1.first, p1.second);
                double dist2 = calculateDistance(p2.first, p2.second);
                return Double.compare(dist1, dist2);
            }
        });
        return routes;
    }
    public static void handleArgs(String[] args){

        //OSM File check
        File osm = new File(args[0]);
        if (!osm.exists()){
            throw new RuntimeException("OSM File (" + args[0] +") does not exist, confirm if path is correct.");
        }else {
            logger.info("Checked : " + args[0]);
        }


        //EPSG format check
        try {
            setTransform(args[3]);
        }catch (Exception e){
            logger.info("Incorrect EPSG format!");
            e.printStackTrace();
        }


        //Start coordinate file check
        File start = new File(args[1]);
        if (!start.exists()){
            throw new RuntimeException(args[1] + " file does not exist, confirm if path is correct.");
        }else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(start));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    ProjCoordinate tempCord = new ProjCoordinate();
                    toWGS84.transform(new ProjCoordinate(Double.parseDouble(split[0]),Double.parseDouble(split[1])),tempCord);
                    from.add(tempCord);
                }
            }catch (Exception e){
                logger.error("Error in start coordinate file!");
                throw new RuntimeException(e);
            }
            logger.info("Processed : " + args[1]);
            logger.info(args[1] + " contained " + from.size() + " entries.");
        }
        File end = new File(args[2]);

        //End coordinate file check
        if (!end.exists()){
            throw new RuntimeException(args[2] + " file does not exist, confirm if path is correct.");
        }else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(end));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    ProjCoordinate tempCord = new ProjCoordinate();
                    toWGS84.transform(new ProjCoordinate(Double.parseDouble(split[0]),Double.parseDouble(split[1])),tempCord);
                    to.add(tempCord);
                }
            }catch (Exception e){
                logger.error("Error in end coordinate file!");
                throw new RuntimeException(e);
            }
            logger.info("Processed : " + args[2]);
            logger.info(args[2] + " contained " + to.size() + " entries.");
        }
    }
    public static void setTransform(String epsgCode){
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem inputFormat = crsFactory.createFromName(epsgCode);
        CoordinateReferenceSystem WGS84 = crsFactory.createFromName("epsg:4326");
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        toWGS84 = ctFactory.createTransform(inputFormat,WGS84);
    }
}

