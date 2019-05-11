package org.requirementsascode;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Part used by the {@link ModelBuilder} to build a {@link Model}.
 *
 * @see UseCase
 * @author b_muth
 */
public class UseCasePart {
	private UseCase useCase;
	private ModelBuilder modelBuilder;
	private Actor defaultActor;

	UseCasePart(UseCase useCase, ModelBuilder modelBuilder) {
		this.useCase = useCase;
		this.modelBuilder = modelBuilder;
		this.defaultActor = modelBuilder.build().getUserActor();
	}

	/**
	 * Start the "happy day scenario" where all is fine and dandy.
	 * 
	 * @return the flow part to create the steps of the basic flow.
	 */
	public FlowPart basicFlow() {
		Flow useCaseFlow = getUseCase().getBasicFlow();
		return new FlowPart(useCaseFlow, this);
	}

	/**
	 * Start a flow with the specified name.
	 * 
	 * @param flowName the name of the flow.
	 * 
	 * @return the flow part to create the steps of the flow.
	 */
	public FlowPart flow(String flowName) {
		Flow useCaseFlow = getUseCase().newFlow(flowName);
		return new FlowPart(useCaseFlow, this);
	}

	/**
	 * Define a default actor that will be used for each step of the use case,
	 * unless it is overwritten by specific actors for the steps (with
	 * <code>as</code>).
	 * 
	 * @param defaultActor the actor to use as a default for the use case's steps
	 * @return this use case part
	 */
	public UseCasePart as(Actor defaultActor) {
		this.defaultActor = defaultActor;
		return this;
	}

	UseCase getUseCase() {
		return useCase;
	}

	ModelBuilder getModelBuilder() {
		return modelBuilder;
	}

	Actor getDefaultActor() {
		return defaultActor;
	}

	/**
	 * Returns the model that has been built.
	 * 
	 * @return the model
	 */
	public Model build() {
		return modelBuilder.build();
	}

	public <T> FlowlessUserPart<T> on(Class<T> eventOrExceptionClass) {
		ConditionPart conditionPart = condition(null);
		FlowlessUserPart<T> flowlessUserPart = conditionPart.on(eventOrExceptionClass);
		return flowlessUserPart;
	}

	public ConditionPart condition(Condition condition) {
		ConditionPart conditionPart = createConditionPart(condition, 1);
		return conditionPart;
	}

	ConditionPart createConditionPart(Condition optionalCondition, long flowlessStepCounter) {
		FlowlessStep newStep = useCase.newFlowlessStep(optionalCondition, "S" + flowlessStepCounter);
		StepPart stepPart = new StepPart(newStep, UseCasePart.this, null);
		ConditionPart conditionPart = new ConditionPart(stepPart, flowlessStepCounter);
		return conditionPart;
	}

	public class ConditionPart {
		private long flowlessStepCounter;
		private StepPart stepPart;

		private ConditionPart(StepPart stepPart, long flowlessStepCounter) {
			this.stepPart = stepPart;
			this.flowlessStepCounter = flowlessStepCounter;
		}

		public <T> FlowlessUserPart<T> on(Class<T> eventOrExceptionClass) {
			StepUserPart<T> stepUserPart = stepPart.on(eventOrExceptionClass);
			FlowlessUserPart<T> flowlessUserPart = new FlowlessUserPart<>(stepUserPart, flowlessStepCounter);
			return flowlessUserPart;
		}

		public <T> FlowlessSystemPart<ModelRunner> system(Runnable systemReaction) {
			StepSystemPart<ModelRunner> stepSystemPart = stepPart.system(systemReaction);
			FlowlessSystemPart<ModelRunner> flowlessSystemPart = new FlowlessSystemPart<>(stepSystemPart, flowlessStepCounter);
			return flowlessSystemPart;
		}

		public <T> FlowlessSystemPart<ModelRunner> system(Consumer<ModelRunner> systemReaction) {
			StepSystemPart<ModelRunner> stepSystemPart = stepPart.system(systemReaction);
			FlowlessSystemPart<ModelRunner> flowlessSystemPart = new FlowlessSystemPart<>(stepSystemPart, flowlessStepCounter);
			return flowlessSystemPart;
		}
	}

	public class FlowlessUserPart<T> {
		private StepUserPart<T> stepUserPart;
		private long flowlessStepCounter;

		private FlowlessUserPart(StepUserPart<T> stepUserPart, long flowlessStepCounter) {
			this.stepUserPart = stepUserPart;
			this.flowlessStepCounter = flowlessStepCounter;
		}

		public FlowlessSystemPart<T> system(Runnable systemReactionObject) {
			StepSystemPart<T> stepSystemPart = stepUserPart.system(systemReactionObject);
			return new FlowlessSystemPart<>(stepSystemPart, flowlessStepCounter);
		}

		public FlowlessSystemPart<T> system(Consumer<T> systemReactionObject) {
			StepSystemPart<T> stepSystemPart = stepUserPart.system(systemReactionObject);
			return new FlowlessSystemPart<>(stepSystemPart, flowlessStepCounter);
		}

		public FlowlessSystemPart<T> systemPublish(Function<T, Object[]> systemReactionObject) {
			StepSystemPart<T> stepSystemPart = stepUserPart.systemPublish(systemReactionObject);
			return new FlowlessSystemPart<>(stepSystemPart, flowlessStepCounter);
		}
	}

	public class FlowlessSystemPart<T> {
		private long flowlessStepCounter;
		private UseCasePart useCasePart;

		private FlowlessSystemPart(StepSystemPart<T> stepSystemPart, long flowlessStepCounter) {
			this.flowlessStepCounter = flowlessStepCounter;
			this.useCasePart = stepSystemPart.getStepPart().getUseCasePart();
		}

		public ConditionPart condition(Condition condition) {
			ConditionPart conditionPart = useCasePart.createConditionPart(condition, ++flowlessStepCounter);
			return conditionPart;
		}

		public <U> FlowlessUserPart<U> on(Class<U> messageClass) {
			FlowlessUserPart<U> flowlessUserPart = condition(null).on(messageClass);
			return flowlessUserPart;
		}

		public Model build() {
			return useCasePart.build();
		}

		public UseCasePart useCase(String useCaseName) {
			Objects.requireNonNull(useCaseName);
			UseCasePart useCasePart = getModelBuilder().useCase(useCaseName);
			return useCasePart;
		}

	}
}
