package liquidwars.model;

  // A single particle on the grid
  // Immutable so that it is easy to reason about in tests for now
public record Particle(int teamId, int energy) {
    public Particle withTeam(int newTeamId)
    {
        return new Particle(newTeamId, energy);
    }

    public Particle withEnergy(int newEnergy)
    {
        return new Particle(teamId, newEnergy);
    }
}
