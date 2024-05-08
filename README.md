# GeoRoot 
<p align="center">
<img src='georoute.png' width='250'>
</p>
GeoRoot is a Java application designed to calculate the distances between coordinates using OpenStreetMap data. It utilizes the open-source routing engine, GraphHopper, to perform offline route calculations. It is designed by Steeve Doss and Byron Xu and under the development of Project Moms in Danger at UT Austin's Innovations for Peace and Development think tank.

&nbsp;

Much of social science research relies on qGIS and STATA plugins for georouting calculations. However, these plugins often depend on extensive third-party API usage. In contrast, GeoRoot offers a solution that meets sensitive data requirements by operating entirely offline. This makes GeoRoot suitable for projects involving protected datasets that cannot be shared. Additionally, with Osmium-tool time-filtering, GeoRoot is capable of analyzing time-series historical data. More information on time-series routing [below](https://github.com/steevedoss/GeoRoot/blob/main/README.md#optimizations).




## Features

GeoRoot is a relatively simple program written to be efficient and accessible to those who aren't familiar with geographic information systems or advanced routing technologies. Its straightforward design enables users, even those without technical expertise, to input geographic data and obtain detailed route information quickly. By abstracting the complexities of data processing and pathfinding algorithms into user-friendly commands, GeoRoot makes geographic route optimization analysis to a broader audience.

- Transform coordinates using the specified EPSG code.
- Calculate shortest paths between sets of start and end points.
- Generate distance matrix and GeoJSON output of the calculated routes.
- Process all data offline without exposing data to third parties.
## Prerequisites

To get up and running with GeoRoute, you need:

- Java Development Kit (JDK) version 11.0.22 ([Available Here](https://www.oracle.com/java/technologies/downloads/#java11)) 
- OpenStreetMap PBF road data ([Available Here](https://download.geofabrik.de/))
- 32GB+ of system memory. Routing engines are memory-intensive applications, sometimes requiring about 32GB+ of RAM for optimal performance. Systems with less than 32GB of system memory may encounter crashes.
 


## Running the Application


Run the application using the following command:


```bash
  java -Xmx32G -jar GeoRoot.jar [network.osm.pbf] [startCoordinates.txt] [endCoordinates.txt] [EPSG Code]
```

**Usage Example**

```bash
  java -Xmx32G -jar GeoRoot.jar OpenStreetMap.osm.pbf startCoordinates.txt endCoordinates.txt EPSG:4326
```


**Command Line Arguments**

- `OpenStreetMap.osm.pbf` -  Path to the OpenStreetMap .pbf file.
- `startCoordinates.txt` -  Path to the text file containing start coordinates. 
- `endCoordinates.txt` -  Path to the text file containing end coordinates.
- `EPSG:4326` - This specifies the coordinate system format for the provided data.

Each coordinate should be on a separate line with longitude and latitude separated by a comma (e.g., `30.31077, -97.73997`)

**EXAMPLE** : startCoordinates.txt

```startCoordinates.txt
  30.285046929284775, -97.73339385397372
  39.73679551330287, -104.97951394713833
  30.16645359278435, -97.51075389043622
  ...
```

**EXAMPLE** : endCoordinates.txt

```endCoordinates.txt
  32.40542941283441,-99.7653590686998
  32.9536677216986,-96.85307685710848
  33.10290636848191,-96.67913171984495
  35.18870226837888,-101.86679454799958
  32.72220802037494,-97.11525655476862
  32.768988551749565,-97.09679096319462
  ...
```

Obviously don't leave the `...` at the end of the file...


## Output


**GeoJSON Files**: Generates GeoJSON files (`.geojson`) for each calculated route. These files are uniquely named based on their hash code and response path, ensuring easy identification. GeoJSON files can be viewed using any compatible viewer such as [QGIS](https://qgis.org/), or web-based mapping tools.

- `routes/` directory: Contains GeoJSON files (`.geojson`) for each calculated route, suitable for visualization.

**Distance Matrix**: Creates a CSV file detailing the calculated routes.

- `DistanceMatrix.csv`: A CSV file containing the following columns:
    - `id`: Identifier
    - `startLongitude`, `startLatitude`: Starting coordinates
    - `endLongitude`, `endLatitude`: Ending coordinates
    - `distance(meters)`: Calculated distance in meters
    - `time(ms)`: Routing time in milliseconds
## Optimizations

It is **HIGHLY** recommended that users strip `HIGHWAY` data from OSM.PBF files to reduce size and resource requirements.

This can be done through the use Osmium-tool
```bash
  osmium tags-filter --overwrite -o [output.osm.pbf] [input.osm.pbf] highway
```

**Usage Example**
```bash
  osmium tags-filter --overwrite -o filtered.osm.pbf us-latest.osm.pbf highway
```

## License

This project is licensed under the [Apache-2.0](http://www.apache.org/licenses/) license. See the LICENSE file for details.
