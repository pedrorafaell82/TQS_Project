package tqs.soundshop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoundshopApplicationTests {

    @Test
    void contextLoads() {
        assertThat(SoundshopApplication.class.getSimpleName())
            .isEqualTo("SoundshopApplication");
    }
}
