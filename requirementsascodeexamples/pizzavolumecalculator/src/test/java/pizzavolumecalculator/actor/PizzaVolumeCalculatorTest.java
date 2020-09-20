package pizzavolumecalculator.actor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.requirementsascode.RecordingActor;

import pizzavolumecalculator.actor.command.CalculateVolume;
import pizzavolumecalculator.actor.command.EnterHeight;
import pizzavolumecalculator.actor.command.EnterRadius;

public class PizzaVolumeCalculatorTest {
  private RecordingActor pizzaVolumeCalculator;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    pizzaVolumeCalculator = 
      RecordingActor.basedOn(new PizzaVolumeCalculator());
  }

  @Test
  public void testBasicFlow() {
    pizzaVolumeCalculator.reactTo(new EnterRadius(4));
    pizzaVolumeCalculator.reactTo(new EnterHeight(5));
    Optional<Double> pizzaVolume = pizzaVolumeCalculator.reactTo(new CalculateVolume());
    assertRecordedStepNames("S1", "S2", "S3", "S4"); 

    assertEquals(251.327, pizzaVolume.get(), 0.01);
  }

  @Test
  public void testBasicFlowTwice() {
    pizzaVolumeCalculator.reactTo(new EnterRadius(2));
    pizzaVolumeCalculator.reactTo(new EnterHeight(3));
    Optional<Double> pizzaVolume = pizzaVolumeCalculator.reactTo(new CalculateVolume());

    assertEquals(37.69, pizzaVolume.get(), 0.01);

    reactAndAssertMessagesAreHandled(new EnterRadius(4), new EnterHeight(5), new CalculateVolume());
    assertRecordedStepNames("S1", "S2", "S3", "S4", "S1", "S2", "S3", "S4");

    assertEquals(251.32, pizzaVolume.get(), 0.01);
  }

  @Test
  public void testIllegalRadius() {
    thrown.expect(IllegalArgumentException.class);
    pizzaVolumeCalculator.reactTo(new EnterRadius(-1));
  }

  @Test
  public void testIllegalHeight() {
    thrown.expect(IllegalArgumentException.class);
    pizzaVolumeCalculator.reactTo(new EnterRadius(5));
    pizzaVolumeCalculator.reactTo(new EnterHeight(-1));
  }
  
  protected void reactAndAssertMessagesAreHandled(Object... messages) {
    pizzaVolumeCalculator.reactTo(messages);
    final Object[] actualMessages = pizzaVolumeCalculator.getRecordedMessages();
    assertArrayEquals(messages, actualMessages);
  }

  protected void assertRecordedStepNames(String... expectedStepNames) {
    String[] actualStepNames = pizzaVolumeCalculator.getRecordedStepNames();
    assertArrayEquals(expectedStepNames, actualStepNames);
  }
}
