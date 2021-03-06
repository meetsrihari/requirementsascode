package org.requirementsascode.builder;

import static org.requirementsascode.builder.FlowPart.buildBasicFlowPart;
import static org.requirementsascode.builder.FlowPart.buildFlowPart;
import static org.requirementsascode.builder.FlowlessConditionPart.flowlessConditionPart;

import java.util.Objects;

import org.requirementsascode.AbstractActor;
import org.requirementsascode.Condition;
import org.requirementsascode.Model;
import org.requirementsascode.UseCase;

/**
 * Part used by the {@link ModelBuilder} to build a {@link Model}.
 *
 * @see UseCase
 * @author b_muth
 */
public class UseCasePart {
	private UseCase useCase;
	private ModelBuilder modelBuilder;
	private AbstractActor defaultActor;

	private UseCasePart(String useCaseName, ModelBuilder modelBuilder) {
		this.modelBuilder = Objects.requireNonNull(modelBuilder);
		final Model model = modelBuilder.getModel();
		this.useCase = model.newUseCase(useCaseName);
		this.defaultActor = model.getUserActor();
	}
	
	static UseCasePart useCasePart(String useCaseName, ModelBuilder modelBuilder) {
		return new UseCasePart(useCaseName, modelBuilder);
	}

	/**
	 * Start the "happy day scenario" where all is fine and dandy.
	 * 
	 * @return the flow part to create the steps of the basic flow.
	 */
	public FlowPart basicFlow() {
		return buildBasicFlowPart(this);
	}

	/**
	 * Start a flow with the specified name.
	 * 
	 * @param flowName the name of the flow.
	 * 
	 * @return the flow part to create the steps of the flow.
	 */
	public FlowPart flow(String flowName) {
		Objects.requireNonNull(flowName);
		return buildFlowPart(flowName, this);
	}

	/**
	 * Define a default actor that will be used for each step of the use case,
	 * unless it is overwritten by specific actors for the steps (with
	 * <code>as</code>).
	 * 
	 * @param defaultActor the actor to use as a default for the use case's steps
	 * @return this use case part
	 */
	public UseCasePart as(AbstractActor defaultActor) {
		Objects.requireNonNull(defaultActor);
		this.defaultActor = defaultActor;
		return this;
	}

	/**
	 * Constrains the condition for triggering a system reaction: only if the
	 * specified condition is true, a system reaction can be triggered.
	 *
	 * @param condition the condition that constrains when the system reaction is
	 *                  triggered
	 * @return the created condition part
	 */
	public FlowlessConditionPart condition(Condition condition) {
		FlowlessConditionPart conditionPart = flowlessConditionPart(condition, this, 1);
		return conditionPart;
	}
	
	/**
	 * Creates a named step.
	 * 
	 * @param stepName the name of the created step
	 * @return the created step part
	 */
	public FlowlessStepPart step(String stepName) {
		FlowlessStepPart stepPart = condition(null).step(stepName);
		return stepPart;
	}

	/**
	 * Defines the type of commands that will cause a system reaction.
	 *
	 * <p>
	 * The system reacts to objects that are instances of the specified class or
	 * instances of any direct or indirect subclass of the specified class.
	 *
	 * @param commandClass the class of commands the system reacts to
	 * @param <T>          the type of the class
	 * @return the created user part
	 */
	public <T> FlowlessUserPart<T> user(Class<T> commandClass) {
		Objects.requireNonNull(commandClass);
		FlowlessUserPart<T> flowlessUserPart = condition(null).user(commandClass);
		return flowlessUserPart;
	}

	/**
	 * Defines the type of events or exceptions that will cause a system reaction.
	 *
	 * <p>
	 * The system reacts to objects that are instances of the specified class or
	 * instances of any direct or indirect subclass of the specified class.
	 *
	 * @param eventOrExceptionClass the class of events the system reacts to
	 * @param <T>                   the type of the class
	 * @return the created user part
	 */
	public <T> FlowlessUserPart<T> on(Class<T> eventOrExceptionClass) {
		Objects.requireNonNull(eventOrExceptionClass);
		FlowlessUserPart<T> flowlessUserPart = condition(null).on(eventOrExceptionClass);
		return flowlessUserPart;
	}

	/**
	 * Returns the model that has been built.
	 * 
	 * @return the model
	 */
	public Model build() {
		return modelBuilder.build();
	}

	UseCase getUseCase() {
		return useCase;
	}

	ModelBuilder getModelBuilder() {
		return modelBuilder;
	}

	AbstractActor getDefaultActor() {
		return defaultActor;
	}
}
