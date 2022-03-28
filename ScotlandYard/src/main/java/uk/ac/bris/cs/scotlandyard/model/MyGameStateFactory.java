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
	public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);


	}

	// TODO
	final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			HashSet<Piece> p = new HashSet<>();
			for (Player player : detectives) {
				p.add(player.piece());
			}
			p.add(mrX.piece());
			ImmutableSet<Piece> Players = ImmutableSet.copyOf(p);
			return Players;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for (Player d : detectives) {
				if (d.piece() == detective) {
					return Optional.of(d.location());
				}
			}
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
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			HashSet<Piece> winner = new HashSet<>();
			ImmutableSet<Piece> w = ImmutableSet.copyOf(winner);
			return w;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		}

		@Override
		public GameState advance(Move move) {
			return null;
		}

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is Empty");
			if (!(mrX.isMrX())) throw new IllegalArgumentException("No Mr X");
			if (detectives.isEmpty()) throw new IllegalArgumentException("detective Empty");
			for (Player x :detectives) {
				if (x.has(Ticket.DOUBLE)) {
					throw new IllegalArgumentException("Detective Double");
				}
			}

		}
			/*private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

				// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
				HashSet<Move> SingleMove = new HashSet<>();

				for(int destination : setup.graph.adjacentNodes(source)) {
					// TODO find out if destination is occupied by a detective
					//  if the location is occupied, don't add to the collection of moves to return
					Player x =getPlayers()
					for (){
						if (destination == getDetectiveLocation(p)){
							Move x = destination;
							Mov.remove();

						}

					}
					for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {

						if (Transport.valueOf(player.tickets().toString())== t){
							Moves.add(destination);
						}
						// TODO find out if the player has the required tickets
						//  if it does, construct a SingleMove and add it the collection of moves to return

					}

					// TODO consider the rules of secret moves here
					//  add moves to the destination via a secret ticket if there are any left with the player
				}
			return Moves;
				// TODO return the collection of moves
			}
		*/
	}

}