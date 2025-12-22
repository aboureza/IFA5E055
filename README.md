# IFA5E055
Projet de Compléments de Programmation Orientée Objet

First Milestone A - Gradient (Distance Map)
Implemented:
- A BFS-based gradient computation that builds a distance map from one or more target cells
- Uses 4-neighbourhood movement 
- Distances are computed as :
    - Target cells have distance 0
    - Neighbour cells increase by +1 per step (shortest-path distance)
    - Obstacles remain unreachable and are assigned INF

Files added:

app/src/main/java/liquid_wars/algo/GradientComputer.java
Implements compute(boolean[][] obstacles, List<Pos> targets) and returns dist[y][x].

app/src/test/java/liquid_wars/algo/GradientComputerTest.java
JUnit tests verifying:

target distance is 0

distances grow in layers correctly

obstacles stay INF

paths go around obstacles / unreachable cells remain INF (if included)

How to run tests:

.\gradlew.bat :app:test