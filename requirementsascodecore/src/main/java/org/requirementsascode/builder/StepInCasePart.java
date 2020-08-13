package org.requirementsascode.builder;

import static org.requirementsascode.builder.StepSystemPart.stepSystemPartWithConsumer;
import static org.requirementsascode.builder.StepSystemPart.stepSystemPartWithFunction;
import static org.requirementsascode.builder.StepSystemPart.stepSystemPartWithRunnable;
import static org.requirementsascode.builder.StepSystemPart.stepSystemPartWithSupplier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.requirementsascode.Condition;
import org.requirementsascode.ModelRunner;
import org.requirementsascode.exception.ElementAlreadyInModel;
import org.requirementsascode.exception.NoSuchElementInModel;
import org.requirementsascode.systemreaction.IgnoresIt;

public class StepInCasePart<T> {
  private StepPart stepPart;

  public StepInCasePart(Condition inCase, StepPart stepPart) {
    this.stepPart = stepPart;
    stepPart.getStep().setCase(inCase);
  }

  /**
   * Defines the system reaction. The system will react as specified to the
   * message passed in, when {@link ModelRunner#reactTo(Object)} is called.
   *
   * @param systemReaction the specified system reaction
   * @return the created system part of this step
   */
  public StepSystemPart<T> system(Consumer<? super T> systemReaction) {
    return stepSystemPartWithConsumer(systemReaction, stepPart);
  }

  /**
   * Defines the system reaction. The system will react as specified, but it will
   * ignore the message passed in, when {@link ModelRunner#reactTo(Object)} is
   * called.
   *
   * @param systemReaction the specified system reaction
   * @return the created system part of this step
   */
  public StepSystemPart<T> system(Runnable systemReaction) {
    return stepSystemPartWithRunnable(systemReaction, stepPart);
  }

  /**
   * Defines the system reaction. The system will react as specified to the
   * message passed in, when you call {@link ModelRunner#reactTo(Object)}. After
   * executing the system reaction, the runner will publish the returned event.
   *
   * @param systemReaction the specified system reaction, that returns an event to
   *                       be published.
   * @return the created system part of this step
   */
  public StepSystemPart<T> systemPublish(Function<? super T, ?> systemReaction) {
    return stepSystemPartWithFunction(systemReaction, stepPart);
  }
  
   /**
   * Defines the system reaction. After executing the system reaction, the runner will publish the returned event.
   *
   * @param systemReaction the specified system reaction, that returns an event to
   *                       be published.
   * @return the created system part of this step
   */
  public StepSystemPart<T> systemPublish(Supplier<?> systemReaction) {
    return stepSystemPartWithSupplier(systemReaction, stepPart);
  }

  /**
   * Creates a new step in this flow, with the specified name, that follows the
   * the step before in sequence. The step before this step has no system reaction,
   * i.e. will just receive the message but do nothing with it.
   *
   * @param stepName the name of the step to be created
   * @return the newly created step
   * @throws ElementAlreadyInModel if a step with the specified name already
   *                               exists in the use case
   */
  public StepPart step(String stepName) {
    return system(new IgnoresIt<>()).step(stepName);
  }

  /**
   * Makes the model runner continue after the specified step.
   *
   * @param stepName name of the step to continue after, in this use case.
   * @return the use case part this step belongs to, to ease creation of further
   *         flows
   * @throws NoSuchElementInModel if no step with the specified stepName is found
   *                              in the current use case
   */
  public UseCasePart continuesAfter(String stepName) {
    return stepPart.continuesAfter(stepName);
  }

  /**
   * Makes the model runner continue at the specified step. No alternative flow
   * starting at the specified step is entered, even if its condition is enabled.
   *
   * @param stepName name of the step to continue at, in this use case.
   * @return the use case part this step belongs to, to ease creation of further
   *         flows
   * @throws NoSuchElementInModel if no step with the specified stepName is found
   *                              in the current use case
   */
  public UseCasePart continuesAt(String stepName) {
    return stepPart.continuesAt(stepName);
  }
}