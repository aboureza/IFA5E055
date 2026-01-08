package liquidwars.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticleTest {

    @Test
    void recordStoresFields() {
        Particle p = new Particle(1, 10);
        assertEquals(1, p.teamId());
        assertEquals(10, p.energy());
    }

    @Test
    void withTeamReturnsNewParticleAndKeepsEnergy() {
        Particle original = new Particle(1, 10);
        Particle changed = original.withTeam(2);

        assertNotSame(original, changed);
        assertEquals(1, original.teamId());
        assertEquals(10, original.energy());

        assertEquals(2, changed.teamId());
        assertEquals(10, changed.energy());
    }

    @Test
    void withEnergyReturnsNewParticleAndKeepsTeam() {
        Particle original = new Particle(1, 10);
        Particle changed = original.withEnergy(99);

        assertNotSame(original, changed);
        assertEquals(1, original.teamId());
        assertEquals(10, original.energy());

        assertEquals(1, changed.teamId());
        assertEquals(99, changed.energy());
    }

    @Test
    void recordEqualityWorks() {
        Particle a = new Particle(1, 10);
        Particle b = new Particle(1, 10);
        Particle c = new Particle(2, 10);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, c);
    }
}
