package no.spk.pensjon.faktura.tidsserie.domain.avregning;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Akseptansetestsuite som verifiserer alle spesifikasjonar definert i form av Cucumber *.feature-filer
 * så lenge dei ligg i samme pakke som suita.
 */
@RunWith(Cucumber.class)
@CucumberOptions(glue = {"no.spk.pensjon.faktura.tidsserie.domain.avregning"})
public class AvregningAT {
}
