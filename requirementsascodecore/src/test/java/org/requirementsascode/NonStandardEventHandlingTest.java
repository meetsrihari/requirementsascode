package org.requirementsascode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.systemreaction.IgnoresIt;

public class NonStandardEventHandlingTest extends AbstractTestCase {
	private String stepName;
	private Optional<? extends Object> optionalCondition;
	private Optional<? extends Object> optionalEvent;
	private Object systemReaction;

	@Before
	public void setup() {
		setupWithRecordingModelRunner();
	}

	@Test
	public void recordsAutonomousSystemReactionStep() {
		stepName = "";

		ReactionAsRunnable reactionAsRunnable = new ReactionAsRunnable();
		Model model = modelBuilder
			.useCase(USE_CASE)
				.basicFlow()
					.step(SYSTEM_DISPLAYS_TEXT).system(reactionAsRunnable)
			.build();
	
		modelRunner.handleWith(recordStepDetails());
		modelRunner.run(model);

		assertRecordedStepNames(SYSTEM_DISPLAYS_TEXT);
		assertEquals(SYSTEM_DISPLAYS_TEXT, stepName);
		assertFalse(optionalEvent.isPresent());
		assertFalse(optionalCondition.isPresent());
		assertEquals(reactionAsRunnable, systemReaction);
	}

	private class ReactionAsRunnable implements Runnable {
		@Override
		public void run() {
		}
	}

	private Consumer<StepToBeRun> recordStepDetails() {
		return stepToBeRun -> {
			stepName = stepToBeRun.getStepName();
			optionalCondition = stepToBeRun.getCondition();
			optionalEvent = stepToBeRun.getMessage();
			systemReaction = stepToBeRun.getSystemReaction();
			stepToBeRun.run();
		};
	}

	@Test
	public void recordsStepWithEvent() {
		ReactionAsConsumer reactionAsConsumer = new ReactionAsConsumer();
		Model model = modelBuilder.useCase(USE_CASE)
			.condition(new AlwaysTrue()).on(EntersText.class).system(reactionAsConsumer)
		.build();
	
		modelRunner.handleWith(recordStepDetails());
		modelRunner.run(model);
		modelRunner.reactTo(entersText());

		Object condition = optionalCondition.get();
		assertTrue(condition instanceof AlwaysTrue);

		Object event = optionalEvent.get();
		assertTrue(event instanceof EntersText);

		assertEquals(reactionAsConsumer, systemReaction);
	}

	private class AlwaysTrue implements Condition {
		@Override
		public boolean evaluate() {
			return true;
		}
	}

	private class ReactionAsConsumer implements Consumer<EntersText> {
		@Override
		public void accept(EntersText t) {
		}
	}

	@Test
	public void recordsEventWithUnhandledEventHandler() {
		Model model = modelBuilder
			.useCase(USE_CASE)
				.on(EntersText.class).system(displaysEnteredText())
		.build();

		modelRunner.handleUnhandledWith(this::eventRecordingEventHandler).run(model).reactTo(entersNumber());

		Object event = optionalEvent.get();
		assertTrue(event instanceof EntersNumber);
	}
	
	private void eventRecordingEventHandler(Object event) {
		this.optionalEvent = Optional.of(event);
	}

	@Test
	public void publishesEvent() {
		List<Object> publishedEvents = new ArrayList<>();
		
		Model model = modelBuilder
			.useCase(USE_CASE)
				.basicFlow()
					.step("S1").on(EntersText.class).systemPublish(super.publishesEnteredTextAsEvent())
					.step("S2").on(String.class).system(new IgnoresIt<>())
				.build();
	
		modelRunner.publishWith(event -> publishedEvents.add(event));
		modelRunner.run(model).reactTo(entersText(), "Some String");

		assertEquals(1, publishedEvents.size());
		assertEquals(EntersText.class, publishedEvents.get(0).getClass());
		assertEquals("S2", modelRunner.getLatestStep().get().getName());
	}
	
	@Test
	public void publishesEventToActor() {
		List<Object> actualRecipients = new ArrayList<>();
		Actor recipient = new Actor("Recipient");
		
		Model model = modelBuilder
			.useCase(USE_CASE)
				.basicFlow()
					.step("S1").on(EntersText.class).systemPublish(super.publishesEnteredTextAsEvent()).to(recipient)
					.step("S2").on(String.class).system(new IgnoresIt<>())
				.build();

		modelRunner.publishWith(event -> {
			Step step = modelRunner.getLatestStep().get();
			step.getPublishTo().ifPresent(to -> actualRecipients.add(to));
		});
		modelRunner.run(model).reactTo(entersText(), "Some String");

		assertEquals(1, actualRecipients.size());
		assertEquals(recipient, actualRecipients.get(0));
		assertEquals("S2", modelRunner.getLatestStep().get().getName());
	}
	
	 @Test
	  public void stopsRunningAndPublishingEventAfterSpecifiedStep() {	    
	    Model model = modelBuilder
	      .useCase(USE_CASE)
	        .basicFlow()
	          .step("S1").on(EntersText.class).systemPublish(super.publishesEnteredTextAsEvent())
	          .step("S2").system(() -> {})
	        .build();
	  
	    modelRunner.handleWith(stopAfterStep("S1"));
	    Optional<Object> response = modelRunner.run(model).reactTo(entersText(), "Some String");

	    assertEquals("S1", modelRunner.getLatestStep().get().getName());
	    assertFalse(response.isPresent());
	    
	  }
	 
	  private Consumer<StepToBeRun> stopAfterStep(String stopAfterStepName) {
	    Objects.requireNonNull(stopAfterStepName);
	    return stepToBeRun -> {
	      stepToBeRun.run();
	      String runStepName = stepToBeRun.getStepName();
	      if(runStepName.equals(stopAfterStepName)) {
	        modelRunner.stop();
	      }
	    };
	  }

}
