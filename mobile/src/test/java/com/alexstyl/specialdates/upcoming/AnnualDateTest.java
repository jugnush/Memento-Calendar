package com.alexstyl.specialdates.upcoming;

import com.alexstyl.specialdates.date.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.alexstyl.specialdates.date.DateConstants.FEBRUARY;
import static com.alexstyl.specialdates.date.DateConstants.MARCH;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AnnualDateTest {

    @Test
    public void differentYearsAreEqual() {
        assertThat(new AnnualDate(Date.on(1, FEBRUARY, 2016))).isEqualTo(
                new AnnualDate(Date.on(1, FEBRUARY, 1990))
        );
    }

    @Test
    public void sameYearsAreEqual() {
        assertThat(new AnnualDate(Date.on(1, FEBRUARY, 2016))).isEqualTo(
                new AnnualDate(Date.on(1, FEBRUARY, 2016))
        );
    }

    @Test
    public void differentDatesAreNotEqual() {
        assertThat(new AnnualDate(Date.on(5, FEBRUARY, 2016))).isNotEqualTo(
                new AnnualDate(Date.on(1, MARCH, 2016))
        );
    }
}
