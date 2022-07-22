# DenForest Algorithm

## Introduction
- [`DenForest`](https://doi.org/10.1145/3514221.3517833) is an incremental density-based clustering algoritm for data streams (sliding window model).   
- It supports `insert`/`batch_insert` and `delete`/`batch_delete` operations to incrementally update clusters over data streams. 
- It supports `labelAndReturn` to compute the labels of the data points in the window. 

## How to Use
### Prerequisite 
- maven 


### Build  
- `mvn package -DskipTests`
- JAR file will be created in the `target` folder. 

### Test
- `mvn test`
- Run several tests using `sample_dataset`.

### Example Code 
You can find example codes in following files. 
- ` src/test/java/example/Denforest_test.java`
- ` src/test/java/example/Denforest_optimized_test.java`

## Directory Overview
```
DenForest-SIGMOD2022
├── pom.xml                                             // for maven
├── README.md
├── sample_dataset                                       
└── src
    ├── main
    │   └── java
    │       └── denforest
    │           ├── datapoint                           // datapoint class
    │           ├── DenForest.java                      // denforest algorithm without optimization
    │           ├── DenForestStrided.java               // denforest algorithm with batch optimization
    │           ├── linkcuttree                         // link-cut tree implementation
    │           ├── rtree                               // rtree implementation
    │           └── unionfind                           // union-find implementation
    └── test
        └── java
            ├── denforest
            │   └── DenForestTest.java                  // test code using below examples
            └── example
                ├── Denforest_test.java                 // example code fore denforest
                └── Denforest_optimized_test.java       // example code for batch-optimized denforest
               

``` 

## Background
- The density-based clustering is utilized for various applications such as hot spot detection or segmentation. To serve those applications in real time, it is desired to update clusters incrementally by capturing only the recent data. 
- The previous incremental density-based clustering algorithms often represent clusters as a graph and suffer serious performance degradation. 
- In order to address the problem of slow deletion, we proposes a novel incremental density-based clustering algorithm called `DenForest`.
- DenForest` can determine efficiently and accurately whether a cluster is to be split by a point removed from the window in logarithmic time.
- With extensive evaluations, it is demonstrated that `DenForest` outperforms the state-of-the-art density-based clustering algorithms significantly and achieves the clustering quality comparable with that of DBSCAN. 

## Reference
Bogyeong Kim, Kyoseung Koo, Undraa Enkhbat, and Bongki Moon. 2022. **DenForest: Enabling Fast Deletion in Incremental Density-Based Clustering over Sliding Windows.** In Proceedings of the 2022 International Conference on Management of Data (SIGMOD ’22), June 12–17, 2022, Philadelphia, PA, USA. ACM, New York, NY, USA, 14 pages. [https://doi.org/10.1145/3514221.3517833](https://doi.org/10.1145/3514221.3517833)
