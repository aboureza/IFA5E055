# IFA5E055
Projet de Compléments de Programmation Orientée Objet

**Gradient (Distance Map)**
Implemented:
- A BFS-based gradient computation that builds a distance map from one or more target cells
- Uses 4-neighbourhood movement 
- Distances are computed as :
    - Target cells have distance 0
    - Neighbour cells increase by +1 per step (shortest-path distance)
    - Obstacles remain unreachable and are assigned INF

Files added:
GradientComputer.java
Implements compute(boolean[][] obstacles, List<Pos> targets) and returns dist[y][x].

GradientComputerTest.java

JUnit tests verifying:
-target distance is 0
-distances grow in layers correctly
-obstacles stay INF
-paths go around obstacles / unreachable cells remain INF (if included)

How to run tests:
.\gradlew.bat :app:test

**Update to Gradient**
The old gradientComputer uses a BFS-based gradient computation that builds a distance map from one or more target cells
Now: **MeshGradientComputer**:
-Computes a distance map like the BFS version, but using 8-neighbourhood (N, S, E, W + diagonals).
-Produces smoother paths around corners compared to pure 4-neighbour BFS.
-Keeps the same conventions:
    -target cells have distance 0
    -distances propagate outward (shortest path)
    -obstacles stay unreachable (INF)

Files added:
    - MeshGradientComputer.java
    - MeshGradientComputerTest.java

**One-step Simulation**

Implemented a one-tick simulator that updates the grid using the gradient.

Each particle chooses an action using the subject’s priority order:
-Move (main → good → acceptable)
-Attack (main → good)
-Transfer energy to ally (main)
-Otherwise do nothing

Added tests verifying:
-correct move / attack / transfer behaviour on small maps
-particle count is preserved
-total energy is preserved

Run with:
.\gradlew.bat :app:test


**Swing GUI + Interactive Targets**

Implemented:
Swing window + rendeering loop

Game loop integration
- Computes gradient maps using GradientComputer
- Applies one simulation step using StepSimulator
- Render the updated grid

Mouse-controlled targets
- Team 0 will follow mouse cursor
- for now, Team 1 only follows right click

Visual Style (for now)
- Gray walls, empty cells are in black
- Particles are drawn using a team base colour with brightness based on energy

New files :
App.java - creates map + launched Swing UI
GameController.java - per-frame logic
GameControllerTest.java
GamePanel.java - rendering + mouse input + loop
GamePanelTest.java
ColourUtil.java - team colours + energy brightness
ColourUtilTest.java
How to run with updates:
.\gradlew.bat :app:run --no-daemon --no-configuration-cache


**TODO**
Refinement:
~~Automatic movement of team 2 (also update Compute gradient so that the particles work around the obstacles)~~
~~Add main screen with settings~~
~~Add Timer 
~~Add percentage bar such as in the game~~
~~Add an end screen (and a leave button on the main game screen)~~
~~Make a 1 to 2 proper maps~~
~ button for maps,
~ button for localplay
~ net game (multithreading?)
~ about button
~ oop features
~ multithreading
~ music
~ fps
~ upload gitlab


**Update 08/01/2026**
-Noticed in the App.java file that makeWalls[][] was not being locally used so I removed it since we have a Level_Loader now
