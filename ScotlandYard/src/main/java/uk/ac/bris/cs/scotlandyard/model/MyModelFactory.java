package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO
		class Mymodel implements Model {
			private Board.GameState MyGame;
			private Set<Observer> Observers;
			private Mymodel() {
				this.MyGame = new MyGameStateFactory().build(setup, mrX, detectives);
				this.Observers = new HashSet<>();
			}

			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return MyGame;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				if(Observers.contains(observer))throw new IllegalArgumentException("duplicate observer");
				if(observer.equals(null))throw new IllegalArgumentException("observer cannot be null");

				Observers.add(observer);

			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				if(observer.equals(null))throw new IllegalArgumentException("observer cannot be null");
				if(!Observers.contains(observer))throw new IllegalArgumentException("observer already unregistered");
				if(Observers.isEmpty()) throw new IllegalArgumentException("observers empty");
				Observers.remove(observer);

			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				ImmutableSet<Observer> newObservers = ImmutableSet.copyOf(Observers);
				return newObservers;
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				// TODO Advance the model with move, then notify all observers of what what just happened.

				MyGame = MyGame.advance(move);
				if(!MyGame.getWinner().isEmpty()){
					for (Observer observer: getObservers()) {
						observer.onModelChanged(MyGame, Observer.Event.GAME_OVER);
					}

				}
				else{
					for (Observer observer: getObservers()) {
						observer.onModelChanged(MyGame, Observer.Event.MOVE_MADE);
					}
				}


				//  you may want to use getWinner() to determine whether to send out Event.MOVE_MADE or Event.GAME_OVER

			}
		}
		return new Mymodel();
	}


}
