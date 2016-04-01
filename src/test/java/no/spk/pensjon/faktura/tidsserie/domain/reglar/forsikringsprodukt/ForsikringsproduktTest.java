package no.spk.pensjon.faktura.tidsserie.domain.reglar.forsikringsprodukt;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * @author Snorre E. Brekke - Computas
 */
@RunWith(Cucumber.class)
@CucumberOptions(glue = {
        "no.spk.pensjon.faktura.tidsserie.domain.avregning",
        "no.spk.pensjon.faktura.tidsserie.domain.at"
})
public class ForsikringsproduktTest {

}