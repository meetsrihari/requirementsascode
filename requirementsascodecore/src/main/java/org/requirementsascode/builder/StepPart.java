package org.requirementsascode.builder;

import static org.requirementsascode.builder.StepAsPart.stepAsPart;

import java.util.Objects;
import java.util.function.Supplier;

import org.requirementsascode.AbstractActor;
import org.requirementsascode.Condition;
import org.requirementsascode.FlowStep;
import org.requirementsascode.Model;
import org.requirementsascode.ModelRunner;
import org.requirementsascode.Step;
import org.requirementsascode.UseCase;
import org.requirementsascode.exception.NoSuchElementInModel;
import org.requirementsascode.flowposition.FlowPosition;
import org.requirementsascode.systemreaction.ContinuesAt;

/**
 * Part used by the {@link ModelBuilder} to build a {@link Model}.
 *
 * @see Step
 * @author b_muth
 */
public class StepPart {
  private Step step;
  private UseCasePart useCasePart;
  private FlowPart flowPart;
  private ModelBuilder modelBuilder;
  private AbstractActor systemActor;
  
  private StepPart(Step step, UseCasePart useCasePart, FlowPart flowPart) {
    this.step = Objects.requireNonNull(step);
    this.useCasePart = Objects.requireNonNull(useCasePart);
    this.modelBuilder = useCasePart.getModelBuilder();
    this.systemActor = modelBuilder.getModel().getSystemActor();
    this.flowPart = flowPart;
  }
  
   static StepPart interruptableFlowStepPart(String stepName, FlowPart flowPart) {
      return interruptableFlowStepPart(stepName, flowPart, null);
    }

  static StepPart interruptableFlowStepPart(String stepName, FlowPart flowPart, Condition optionalCondition) {
    UseCasePart useCasePart = flowPart.getUseCasePart();
    UseCase useCase = useCasePart.getUseCase();
    Step step = useCase.newInterruptableFlowStep(stepName, flowPart.getFlow(), optionalCondition);
    return new StepPart(step, useCasePart, flowPart);
  }

  static StepPart interruptingFlowStepPart(String stepName, FlowPart flowPart, FlowPosition flowPosition,
    Condition optionalCondition) {
    UseCasePart useCasePart = flowPart.getUseCasePart();
    UseCase useCase = useCasePart.getUseCase();
    Step step = useCase.newInterruptingFlowStep(stepName, flowPart.getFlow(), flowPosition, optionalCondition);
    return new StepPart(step, useCasePart, flowPart);
  }
  
  static StepPart stepPartWithoutFlow(String stepName, UseCasePart useCasePart, Condition optionalCondition) {
    Step step = useCasePart.getUseCase().newFlowlessStep(stepName, optionalCondition);
    return new StepPart(step, useCasePart, null);
  }
  
  /**
   * Immediately before a step is run, the specified case condition is checked. If
   * the condition evaluates to true, the model runner runs the step. If it
   * evauluates to false, the model runner proceeds to the next step in the same
   * flow.
   * 
   * @param aCase the case conditon
   * @return the created in case part of this step
   */
  public StepInCasePart<?> inCase(Condition aCase) {
    return as(systemActor).user(ModelRunner.class).inCase(aCase);
  }

  /**
   * Defines which actors (i.e. user groups) can cause the system to react to the
   * message of this step.
   *
   * @param actors the actors that define the user groups
   * @return the created as part of this step
   */
  public StepAsPart as(AbstractActor... actors) {
    Objects.requireNonNull(actors);
    return stepAsPart(actors, this);
  }

  /**
   * Defines the type of user command objects that this step accepts. Commands of
   * this type can cause a system reaction.
   *
   * <p>
   * Given that the step's condition is true, and the actor is right, the system
   * reacts to objects that are instances of the specified class or instances of
   * any direct or indirect subclass of the specified class.
   *
   * @param commandClass the class of commands the system reacts to in this step
   * @param <T>          the type of the class
   * @return the created user part of this step
   */
  public <T> StepUserPart<T> user(Class<T> commandClass) {
    Objects.requireNonNull(commandClass);
    AbstractActor defaultActor = getUseCasePart().getDefaultActor();
    StepUserPart<T> userPart = as(defaultActor).user(commandClass);
    return userPart;
  }

  /**
   * Defines the type of system event objects or exceptions that this step
   * handles. Events of the specified type can cause a system reaction.
   *
   * <p>
   * Given that the step's condition is true, and the actor is right, the system
   * reacts to objects that are instances of the specified class or instances of
   * any direct or indirect subclass of the specified class.
   *
   * @param eventOrExceptionClass the class of events the system reacts to in this
   *                              step
   * @param <T>                   the type of the class
   * @return the created user part of this step
   */
  public <T> StepUserPart<T> on(Class<T> eventOrExceptionClass) {
    Objects.requireNonNull(eventOrExceptionClass);
    StepUserPart<T> userPart = as(systemActor).user(eventOrExceptionClass);
    return userPart;
  }

  /**
   * Defines an "autonomous system reaction", meaning the system will react
   * without needing a message provided via {@link ModelRunner#reactTo(Object)}.
   *
   * @param systemReaction the autonomous system reaction
   * @return the created system part of this step
   */
  public StepSystemPart<ModelRunner> system(Runnable systemReaction) {
    Objects.requireNonNull(systemReaction);
    StepSystemPart<ModelRunner> systemPart = as(systemActor).system(systemReaction);
    return systemPart;
  }

  /**
   * Defines an "autonomous system reaction", meaning the system will react
   * without needing a message provided via {@link ModelRunner#reactTo(Object)}.
   * After executing the system reaction, the runner will publish the returned
   * event.
   *
   * @param systemReaction the autonomous system reaction, that returns a single
   *                       event to be published.
   * @return the created system part of this step
   */
  public StepSystemPart<ModelRunner> systemPublish(Supplier<?> systemReaction) {
    Objects.requireNonNull(systemReaction);
    StepSystemPart<ModelRunner> systemPart = as(systemActor).systemPublish(systemReaction);
    return systemPart;
  }

  /**
   * Positions the modelRunner after the specified step, and reevaluates which flows can start from there.
   *
   * @param stepName name of the step to continue after, in this use case.
   * @return the use case part this step belongs to, to ease creation of further
   *         flows
   * @throws NoSuchElementInModel if no step with the specified stepName is found
   *                              in the current use case
   */
  public UseCasePart continuesAfter(String stepName) {
    Objects.requireNonNull(stepName);
    UseCasePart useCasePart = as(systemActor).continuesAfter(stepName);
    return useCasePart;
  }

  /**
   * Makes the model runner continue at the specified step. 
   * 
   * IMPORTANT NOTE: given you have specified continuesAt(x), 
   * if there is an alternative flow with an insteadOf(x) condition,
   * that alternative flow will be given preference to x if its condition is fulfilled.
   *
   * @param stepName name of the step to continue at, in this use case.
   * @return the use case part this step belongs to, to ease creation of further
   *         flows
   * @throws NoSuchElementInModel if no step with the specified stepName is found
   *                              in the current use case
   */
  public UseCasePart continuesAt(String stepName) {
    Objects.requireNonNull(stepName);
    user(ModelRunner.class).system(new ContinuesAt<>(stepName, (FlowStep) step));
    return useCasePart;
  }

  Step getStep() {
    return step;
  }

  FlowPart getFlowPart() {
    return flowPart;
  }

  UseCasePart getUseCasePart() {
    return useCasePart;
  }

  ModelBuilder getModelBuilder() {
    return modelBuilder;
  }
}