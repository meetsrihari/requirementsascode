package org.requirementsascode.builder;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.requirementsascode.Actor;
import org.requirementsascode.Condition;
import org.requirementsascode.Model;
import org.requirementsascode.ModelRunner;
import org.requirementsascode.Step;
import org.requirementsascode.exception.NoSuchElementInModel;
import org.requirementsascode.flowposition.FlowPosition;

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
	private Actor systemActor;

	/**
	 * Creates a step in a use case flow that can be interrupted by other flows that
	 * start.
	 * 
	 * @param stepName the name of the step
	 * @param flowPart the flow part used to create the flow
	 */
	StepPart(String stepName, FlowPart flowPart) {
		this.useCasePart = flowPart.getUseCasePart();
		this.modelBuilder = useCasePart.getModelBuilder();
		this.systemActor = modelBuilder.build().getSystemActor();
		this.flowPart = flowPart;
		this.step = useCasePart.getUseCase().newInterruptableFlowStep(stepName, flowPart.getFlow());
	}

	/**
	 * Creates a conditional step at the beginning of a flow that can interrupt
	 * other flows.
	 * 
	 * @param step
	 * @param flowPart
	 */
	StepPart(String stepName, FlowPart flowPart, FlowPosition flowPosition, Condition optionalCondition) {
		this.useCasePart = flowPart.getUseCasePart();
		this.flowPart = flowPart;
		this.modelBuilder = useCasePart.getModelBuilder();
		this.systemActor = modelBuilder.build().getSystemActor();
		this.step = useCasePart.getUseCase().newInterruptingFlowStep(stepName, flowPart.getFlow(), flowPosition,
			optionalCondition);
	}

	/**
	 * Creates a step, without a use case flow.
	 * 
	 * @param stepName the name of the step
	 * @param useCasePart the use case of the step
	 * @param optionalCondition a condition for the step, or null if the step is unconditional
	 */
	StepPart(String stepName, UseCasePart useCasePart, Condition optionalCondition) {
		this.useCasePart = useCasePart;
		this.modelBuilder = useCasePart.getModelBuilder();
		this.systemActor = modelBuilder.build().getSystemActor();
		this.step = useCasePart.getUseCase().newFlowlessStep(stepName, optionalCondition);
	}

	/**
	 * Defines which actors (i.e. user groups) can cause the system to react to the
	 * message of this step.
	 *
	 * @param actors the actors that define the user groups
	 * @return the created as part of this step
	 */
	public StepAsPart as(Actor... actors) {
		Objects.requireNonNull(actors);
		return new StepAsPart(actors, this);
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
		Actor defaultActor = getUseCasePart().getDefaultActor();
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
	 * Instead, the model runner provides itself as an event to the system reaction.
	 *
	 * @param systemReaction the autonomous system reaction (that needs information
	 *                       from a model runner to work)
	 * @return the created system part of this step
	 */
	public StepSystemPart<ModelRunner> system(Consumer<ModelRunner> systemReaction) {
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
	 * Makes the model runner continue after the specified step.
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
	 * Makes the model runner continue at the specified step. If there are
	 * alternative flows starting at the specified step, one may be entered if its
	 * condition is enabled.
	 *
	 * @param stepName name of the step to continue at, in this use case.
	 * @return the use case part this step belongs to, to ease creation of further
	 *         flows
	 * @throws NoSuchElementInModel if no step with the specified stepName is found
	 *                              in the current use case
	 */
	public UseCasePart continuesAt(String stepName) {
		Objects.requireNonNull(stepName);
		UseCasePart useCasePart = as(systemActor).continuesAt(stepName);
		return useCasePart;
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
	public UseCasePart continuesWithoutAlternativeAt(String stepName) {
		Objects.requireNonNull(stepName);
		UseCasePart useCasePart = as(systemActor).continuesWithoutAlternativeAt(stepName);
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
