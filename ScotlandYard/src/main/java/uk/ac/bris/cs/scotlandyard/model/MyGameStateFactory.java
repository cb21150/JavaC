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
		public static ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private Integer round;



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
			if(piece.isMrX()) {
				return Optional.of(ticket -> mrX.tickets().get(ticket));
			}
			for(Player d: detectives){
				if(d.piece()==piece) {

					return Optional.of(ticket -> d.tickets().get(ticket));
				}
				}
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

			HashSet<SingleMove> moves1 = new HashSet<>();
			HashSet<Move> moves2 = new HashSet<>();
			HashSet<Move> moves = new HashSet<>();
			for (Player d  : detectives){
				if(remaining.contains(d.piece())){
					moves1 = (HashSet<SingleMove>) makeSingleMoves(setup, detectives, d, d.location());
					moves.addAll(moves1);
					System.out.println(detectives);
					System.out.println(remaining);
					System.out.println(moves1);
				}
			}
			if (remaining.contains(mrX.piece())) {
				moves2=(HashSet<Move>) makeDoubleMoves(setup, detectives, mrX, mrX.location());
				moves.addAll(moves2);

			}
			moves.addAll(moves2);
			ImmutableSet<Move>Moves = ImmutableSet.copyOf(moves);
			return Moves;




		}
		@Override
		public GameState advance(Move move) {
			moves = getAvailableMoves();
			if(!moves.contains(move))throw new IllegalArgumentException("Illegal move "+ move);

			class moveType implements Visitor<GameState> {

				@Override
				public GameState visit(DoubleMove MoveD) {
					ImmutableList<LogEntry> log1;
					log1 = updatelog(MoveD.ticket1,MoveD.destination1,log);
					log1 = updatelog(MoveD.ticket2,MoveD.destination2, log1);

					Player player = mrX.at(MoveD.destination1);
					player.at(MoveD.destination2);
					player.use(MoveD.tickets());
					ImmutableSet Finalremaining = updateremaining(remaining, mrX.piece());
					return new MyGameState(setup, Finalremaining, log1, player, detectives);
				}

				@Override
				public GameState visit(SingleMove MoveS) {
					if (move.commencedBy().isMrX()) {
						ImmutableList<LogEntry> log1;
						log1 = updatelog(MoveS.ticket,MoveS.destination, log);
						Player player = mrX.at(MoveS.destination);
						player.use(MoveS.ticket);
						ImmutableSet Finalremaining= updateremaining(remaining, mrX.piece());
						return new MyGameState(setup, Finalremaining, log1, player, detectives);
					} else {

						for (Player d : detectives) {
							if (move.commencedBy().webColour() == d.piece().webColour()) {

								d.at(MoveS.destination);
								d.use(MoveS.ticket);
								mrX.give(MoveS.ticket);
								ImmutableSet<Piece> remaining2 = updateremaining(remaining, d.piece());
								return new MyGameState(setup, remaining2, log, mrX, detectives);
							}

						}
						return new MyGameState(setup, remaining, log, mrX, detectives);
					}

				}
			}
			return move.accept(new moveType());
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
			for (Player d :detectives) {
				if (d.has(Ticket.SECRET)) {
					throw new IllegalArgumentException("Detectives have secret tickets");
				}
			}
			for(Player d: detectives){
				int indexd = detectives.indexOf(d);
				for(Player d2: detectives.subList(indexd+1, detectives.size())){
					if (d.location()== d2.location()){
						throw new IllegalArgumentException("detectives overlap");
					}
				}
			}
			if(setup.graph.edges().isEmpty())throw new IllegalArgumentException("graph is empty");

		}

			private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

				// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
				HashSet<SingleMove> Moves = new HashSet<>();

				for(int destination : setup.graph.adjacentNodes(source)) {
					// TODO find out if destination is occupied by a detective
					//  if the location is occupied, don't add to the collection of moves to return
					boolean available = true;
					for(Player d: detectives) {
						if (destination == d.location()) {
							available = false;
						}
					}
					for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
						// TODO find out if the player has the required tickets
						//  if it does, construct a SingleMove and add it the collection of moves to return
						if (player.has(t.requiredTicket()) && available) {
							Moves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}

						// TODO consider the rules of secret moves here
						//  add moves to the destination via a secret ticket if there are any left with the player
						if (player.has(Ticket.SECRET) && available) {
							Moves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
						}
					}
				}
				return Moves;
				// TODO return the collection of moves
			}

		private static Set<Move> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			HashSet<SingleMove> moves1;
			moves1 = (HashSet<SingleMove>) makeSingleMoves(setup, detectives, player, source);
			Set<SingleMove> Moves2 = new HashSet<>();
			Set<DoubleMove> Moves = new HashSet<>();
				if (player.has(Ticket.DOUBLE)&& setup.moves.size()>=2) {
					for (SingleMove move1 : moves1) {
						Moves2 = makeSingleMoves(setup, detectives, player, move1.destination);
						for (SingleMove move2 : Moves2) {
							if (!(move1.ticket.name().equals(move2.ticket.name())) || player.hasAtLeast(move1.ticket, 2)) {

								Moves.add(new DoubleMove(player.piece(), source,
										move1.ticket, move1.destination,
										move2.ticket, move2.destination));
							}
						}
					}
				}
			HashSet<Move> allmoves = new HashSet<>();
			for(Move m: moves1) allmoves.add(m);
			for(Move m: Moves) allmoves.add(m);
		return allmoves;
		}
		private ImmutableList<LogEntry> updatelog(Ticket t, int d, ImmutableList<LogEntry> log2){
			List<LogEntry> templog = new ArrayList<>(log2);
			if (setup.moves.get(log.size()) == true) {
				templog.add(LogEntry.reveal(t,d));
			}
			if (!setup.moves.get(log.size())) {
				templog.add(LogEntry.hidden(t));
			}
			ImmutableList<LogEntry> log1 = ImmutableList.copyOf(templog);
			System.out.println(log1);
			return log1;
		}
		private ImmutableSet<Piece> updateremaining(ImmutableSet<Piece> remain,Piece p){
			HashSet<Piece> remainingd = new HashSet<>(remain);
			if (remain.contains(mrX.piece())){
				remainingd.addAll(getPlayers());
			}
			remainingd.remove(p);
			if (remainingd.isEmpty()){
				remainingd.add(mrX.piece());
			}
			ImmutableSet<Piece> Finalremaining = ImmutableSet.copyOf(remainingd);
			return Finalremaining;
		}


	}

}