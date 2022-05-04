package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

import com.google.common.collect.Iterables;
import jdk.jfr.Frequency;
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
				return Optional.of(new TicketBoard() {
					@Override
					public int getCount(@Nonnull Ticket ticket) {
						return mrX.tickets().get(ticket);
					}
				});
			}
			for(Player d: detectives){
				if(d.piece()==piece) {
					return Optional.of(new TicketBoard() {
						@Override
						public int getCount(@Nonnull Ticket ticket) {
							return d.tickets().get(ticket);
						}
					});
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
			return winner;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			HashSet<Move> moves = new HashSet<>();
			if (remaining.contains(mrX.piece())) {
				moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
			}
			else {
				for (Player d : detectives) {
					if (remaining.contains(d.piece())) {
						moves.addAll(makeSingleMoves(setup, detectives, d, d.location()));
					}
				}
			}
			ImmutableSet<Move>Moves = ImmutableSet.copyOf(moves);
			return Moves;




		}
		@Override
		public GameState advance(Move move) {
			if(!moves.contains(move))throw new IllegalArgumentException("Illegal move "+ move);
			class moveType implements Visitor<GameState> {

				@Override
				public GameState visit(DoubleMove MoveD) {
					ImmutableList<LogEntry> log1;
					log1 = updatelog(MoveD.ticket1,MoveD.destination1,log);
					System.out.println(log1);
					ImmutableList<LogEntry> log2 = updatelog(MoveD.ticket2,MoveD.destination2, log1);
					System.out.println(log2);
					mrX= mrX.at(MoveD.destination1);
					mrX = mrX.at(MoveD.destination2);
					mrX = mrX.use(MoveD.tickets());
					ImmutableSet Finalremaining = updateremaining(remaining, mrX.piece());
					return new MyGameState(setup, Finalremaining, log2, mrX, detectives);
				}

				@Override
				public GameState visit(SingleMove MoveS) {
					if (move.commencedBy().isMrX()) {
						ImmutableList<LogEntry> log1;
						log1 = updatelog(MoveS.ticket,MoveS.destination, log);
						mrX = mrX.at(MoveS.destination);
						mrX = mrX.use(MoveS.ticket);
						ImmutableSet Finalremaining= updateremaining(remaining, mrX.piece());
						return new MyGameState(setup, Finalremaining, log1, mrX, detectives);
					}else{
						List<Player> newDetectives = new ArrayList<>(detectives);
						ImmutableSet<Piece> Finalremaining = updateremaining(remaining,MoveS.commencedBy());
						for (Player d : detectives) {
							if (move.commencedBy()== d.piece()) {
								Player newD = d.use (MoveS.ticket);
								newD = newD.at(MoveS.destination);
								newDetectives.set(detectives.indexOf(d), newD);
							}
						}
						mrX = mrX.give(MoveS.ticket);
						return new MyGameState(setup, Finalremaining,log,mrX,newDetectives);
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
			this.moves = getAvailableMoves();
			this.winner = ImmutableSet.of();

			//----------------------This is where im trying to work out winner --------------------------
			HashSet<Piece> winners = new HashSet<>();
			HashSet<Player> deadPlayers = new HashSet<>();
			System.out.println(this.remaining);
			for (Player d :this.detectives) {
				//to see if mrx is in the same location as the detective
				if (mrX.location() == d.location()) {
					for (Player p : this.detectives) {
						winners.add(p.piece());
					}
				}
				if (Iterables.all(d.tickets().values(), (Integer e) -> e == 0)) {        //this is from https://stackoverflow.com/questions/37950780/java-clear-the-list-if-all-elements-are-zero
					deadPlayers.add(d);
					if (deadPlayers.size() == detectives.size()) {
						winners.add(this.mrX.piece());
					}

				}
				if (remaining.contains(mrX.piece()) && moves.isEmpty()){
					winners.add(this.mrX.piece());
				}
				if (this.log.size() == this.setup.moves.size()){
					winners.add(this.mrX.piece());
				}
			}


			this.winner = ImmutableSet.copyOf(winners);

			if ( !winner.isEmpty()){
				this.remaining = ImmutableSet.of();
			}

			//-------------------------------------------------------------------------------------
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is Empty");
			if (!(mrX.isMrX())) throw new IllegalArgumentException("No Mr X");
			if (detectives.isEmpty()) throw new IllegalArgumentException("detective Empty");
			for (Player d :detectives) {
				if (d.has(Ticket.DOUBLE)) {
					throw new IllegalArgumentException("Detective Double");
				}
				if (d.has(Ticket.SECRET)) {
					throw new IllegalArgumentException("Detectives have secret tickets");
				}
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
			if (setup.moves.get(log2.size())) {
				templog.add(LogEntry.reveal(t,d));
			}else{

				templog.add(LogEntry.hidden(t));
			}
			ImmutableList<LogEntry> log1 = ImmutableList.copyOf(templog);

			return log1;
		}
		private ImmutableSet<Piece> updateremaining(ImmutableSet<Piece> remain,Piece p){
			HashSet<Piece> remainingd = new HashSet<>(remain);
			if (remain.contains(mrX.piece())){
				for (Player d :detectives){
					remainingd.add(d.piece());
				}
			}
			remainingd.remove(p);

			if (remainingd.isEmpty()){
				remainingd.add(mrX.piece());
			}

			HashSet<Piece> deadDetectives = new HashSet<>();
			for (Player d : detectives){
				if (makeSingleMoves(setup, detectives, d, d.location()).isEmpty()){
					deadDetectives.add(d.piece());
				}
			}
			if (!(deadDetectives.size() == detectives.size()) && (remainingd.equals(deadDetectives))){
				remainingd.removeAll(deadDetectives);
				remainingd.add(mrX.piece());
			}
			ImmutableSet<Piece> Finalremaining = ImmutableSet.copyOf(remainingd);
			return Finalremaining;
		}
	}

}