package org.georoot.RoutingEngine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.Pair;
import com.graphhopper.util.*;
import org.apache.commons.io.FileUtils;
import org.locationtech.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;


public class GHRoutingEngine {

    public static Logger logger = LoggerFactory.getLogger(GHRoutingEngine.class);
    public static GraphHopper hopper;
    public static final int cores = 1; //Experiment at your own peril
    public GHRoutingEngine(){
        logger.info("CPU Threads : " + cores);
        logger.info("Memory Allocated : " + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()));
        logger.warn("Routing engines are memory intensive applications, sometimes requiring about 32GB+ of RAM for optimal performance.");
        File routesDir = new File("routes/");
        if (!routesDir.exists()){
            if (routesDir.mkdir()){
                logger.info("Successfully created routes/");
            }
        }
    }
    public void close(){
        hopper.close();
    }
    public void setOSMFile(String osmFile){
        hopper = createGraphHopperInstance(osmFile);
    }
    public GraphHopper createGraphHopperInstance(String ghLoc) {

        logger.info("Caching : "+ghLoc + " (caching will occur only once)");
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc); //Load osmFile
        hopper.setGraphHopperLocation("target/routing-graph-cache"); //Cache directory(delete if corrupted)
        hopper.setProfiles(new Profile("car").setVehicle("car").setTurnCosts(false));
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
        hopper.importOrLoad();
        return hopper;
    }
    public Pair<Double,Long> route(ProjCoordinate start,ProjCoordinate end) {

        GHRequest req = new GHRequest(start.x,start.y, end.x, end.y).
                // note that we have to specify which profile we are using even when there is only one like here
                        setProfile("car")
                // define the language for the turn instructions
                .setLocale(Locale.US)
                .setAlgorithm(Parameters.Algorithms.ASTAR)
                .putHint(Parameters.CH.DISABLE, true);
        GHResponse rsp = hopper.route(req);

        // handle errors
        if (!rsp.hasErrors()){
            // use the best path, see the GHResponse class for more possibilities.
            ResponsePath path = rsp.getBest();
            try {
                Files.write(Paths.get("routes/"+path.hashCode()+".geojson"), pathToGeoJSON(path).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // points, distance in meters and time in millis of the full path
            PointList pointList = path.getPoints();
            double distance = path.getDistance();
            long timeInMs = path.getTime();
            return new Pair<>(distance,timeInMs);
        }else {
            logger.error("Connection between locations not found " + start + " -> " + end);
        }
        return new Pair<>(-1.0,0L);
    }
    public String pathToGeoJSON(ResponsePath path) {
        PointList pointList = path.getPoints();
        JsonArray coordinates = new JsonArray();

        for (int i = 0; i < pointList.size(); i++) {
            JsonArray point = new JsonArray();
            point.add(pointList.getLon(i));
            point.add(pointList.getLat(i));
            coordinates.add(point);
        }

        JsonObject geometry = new JsonObject();
        geometry.addProperty("type", "LineString");
        geometry.add("coordinates", coordinates);

        JsonObject feature = new JsonObject();
        feature.addProperty("type", "Feature");
        feature.add("geometry", geometry);

        JsonArray features = new JsonArray();
        features.add(feature);

        JsonObject geoJson = new JsonObject();
        geoJson.addProperty("type", "FeatureCollection");
        geoJson.add("features", features);

        Gson gson = new Gson();
        return gson.toJson(geoJson);
    }

}

