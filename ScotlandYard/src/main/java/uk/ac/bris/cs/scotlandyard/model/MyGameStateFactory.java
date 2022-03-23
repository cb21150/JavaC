package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull
	@Override
	public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		final class MyGameState implements GameState {
			public MyGameState(GameSetup setup, ImmutableSet<Piece> of, ImmutableList<LogEntry> of1, Player mrX, ImmutableList<Player> detectives) {
			}

			@Override
			public GameSetup getSetup() {
				return null;
			}

			@Override
			public ImmutableSet<Piece> getPlayers() {
				return null;
			}

			@Nonnull
			@Override
			public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				return Optional.empty();
			}

			@Nonnull
			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				return Optional.empty();
			}

			@Nonnull
			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getWinner() {
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Move> getAvailableMoves() {
				return null;
			}

			@Override
			public GameState advance(Move move) {
				return null;
			}

			private GameSetup setup;
			private ImmutableSet<Piece> remaining;
			private ImmutableList<LogEntry> log;
			private Player mrX;
			private List<Player> detectives;
			private ImmutableSet<Move> moves;
			private ImmutableSet<Piece> winner;

			private MyGameState(final GameSetup setup,
								final ImmutableSet<Piece> remaining,
								final ImmutableList<LogEntry> log,
								final Player mrX, List<Player> detectives,
								final ImmutableSet<Move> moves,
								final ImmutableSet<Piece> winner) {
				this.setup = setup;
				this.remaining = remaining;
				this.log = log;
				this.mrX = mrX;
				this.detectives = detectives;
				this.moves = moves;
				this.winner = winner;
			}
		}
		if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
		if(!(mrX.isMrX())) throw new IllegalArgumentException("No Mr X!");
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}
}
