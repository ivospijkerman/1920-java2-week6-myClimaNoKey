package com.soricosoft.climapm;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

// Use alt-enter to help you with the static imports!

@RunWith(AndroidJUnit4.class)
public class WeatherControllerEspressoTest {

    @Rule
    public ActivityTestRule<WeatherController> activityRule =
            new ActivityTestRule<>(WeatherController.class);

    @Test
    public void ensureCityChangeOpens() throws InterruptedException {
        // Arrange
        String desiredCity = "Amsterdam";

        // Act
        onView(withId(R.id.changeCityButton)).perform(click());
        onView(withId((R.id.queryET)))
                .perform(replaceText(desiredCity),
                        typeText("\n"));

        // Wait a second and then refresh the page, for unknown reasons....
        Thread.sleep(1_000);
        onView(withId(R.id.changeCityButton)).perform(click());
        pressBack();

        // Assert
        onView(withId(R.id.locationTV)).check(matches(withText(desiredCity)));
    }
}
