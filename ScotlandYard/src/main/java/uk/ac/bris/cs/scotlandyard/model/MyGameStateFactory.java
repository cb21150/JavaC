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
			if (remaining.contains(mrX.piece())) { //checks if it is Mrx's turn
				moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location())); //mrx can do doublemoves so make double moves is usdc
			}
			else {
				for (Player d : detectives) {
					if (remaining.contains(d.piece())) {
						moves.addAll(makeSingleMoves(setup, detectives, d, d.location()));//detectives can't do doublemoves so makesinglemoves is used
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
					ImmutableList<LogEntry> log2 = updateLog(MoveD.ticket2,MoveD.destination2, updateLog(MoveD.ticket1,MoveD.destination2,log)); //updates the log twice
					mrX = mrX.at(MoveD.destination2);
					mrX = mrX.use(MoveD.tickets());
					ImmutableSet Finalremaining = updateRemaining(remaining, mrX.piece());
					return new MyGameState(setup, Finalremaining, log2, mrX, detectives);
				}

				@Override
				public GameState visit(SingleMove MoveS) {
					if (move.commencedBy().isMrX()) {
						ImmutableList<LogEntry> log1;
						log1 = updateLog(MoveS.ticket,MoveS.destination, log);
						mrX = mrX.at(MoveS.destination);
						mrX = mrX.use(MoveS.ticket);
						ImmutableSet Finalremaining= updateRemaining(remaining, mrX.piece());
						return new MyGameState(setup, Finalremaining, log1, mrX, detectives);
					}else{
						List<Player> newDetectives = new ArrayList<>(detectives);
						ImmutableSet<Piece> Finalremaining = updateRemaining(remaining,MoveS.commencedBy());
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

			//----------------------This is where winner is worked out --------------------------
			HashSet<Piece> winners = new HashSet<>();
			HashSet<Player> deadPlayers = new HashSet<>();
			for (Player d : detectives) {
				//to see if mrx is in the same location as the detective
				if (mrX.location() == d.location())  {
					for (Player p : detectives) {
						winners.add(p.piece());
					}
				}
				//checks if the detective doesn't have any tickets (all their values equal to zero)
				if (d.tickets().values().stream().allMatch((Integer el) -> el == 0)){
					deadPlayers.add(d);  //if they don't have any tickets then add them to a list of they dead/can no longer move detectives
					if (deadPlayers.size() == detectives.size()) {
						//if all detectives cant move mrX wins
						winners.add(mrX.piece());
					}

				}
				if (remaining.contains(mrX.piece()) && moves.isEmpty()){
					detectives.forEach (det -> winners.add(det.piece()));
				}
				if (this.log.size() == setup.moves.size()){
					winners.add(mrX.piece());
				}
			}
			this.winner = ImmutableSet.copyOf(winners);
			if ( !winner.isEmpty()){						//this checks for a winner and if there is one it makes remaining empty so that
				this.remaining = ImmutableSet.of();			//there will be no more moves in getAvailableMoves
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

		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){//gets all the available singlemoves for player given

			HashSet<SingleMove> Moves = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {

				boolean available = true;
				for(Player d: detectives) {
					if (destination == d.location()) {
						available = false;
					}
				}
				for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {

					if (player.has(t.requiredTicket()) && available) {
						Moves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}

					if (player.has(Ticket.SECRET) && available) {
						Moves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
					}
				}
			}
			return Moves;
		}

		private static Set<Move> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) { //gets available moves including doublemoves
			HashSet<SingleMove> moves1;
			HashSet<Move> allmoves = new HashSet<>();
			moves1 = (HashSet<SingleMove>) makeSingleMoves(setup, detectives, player, source);
			for(Move m: moves1) allmoves.add(m); //adds all mrx's single moves
			Set<SingleMove> Moves2;
			if (player.has(Ticket.DOUBLE)&& setup.moves.size()>=2) {        //checks to see if mrX has a double ticket and also has enough moves left
				for (SingleMove move1 : moves1) {
					Moves2 = makeSingleMoves(setup, detectives, player, move1.destination);
					for (SingleMove move2 : Moves2) {
						//checks that the double move doesn't use the same ticket or if it does mrX has at least 2 of them
						if (!(move1.ticket.name().equals(move2.ticket.name())) || player.hasAtLeast(move1.ticket, 2)) {

							allmoves.add(new DoubleMove(player.piece(), source,
									move1.ticket, move1.destination,
									move2.ticket, move2.destination)); //adds mrx's double move
						}
					}
				}
			}
			return allmoves;
		}
		//Helper Functions
		private ImmutableList<LogEntry> updateLog(Ticket t, int d, ImmutableList<LogEntry> log2){
			List<LogEntry> templog = new ArrayList<>(log2);
			if (setup.moves.get(log2.size())) {
				templog.add(LogEntry.reveal(t,d));
			}else{
				templog.add(LogEntry.hidden(t));
			}
			ImmutableList<LogEntry> log1 = ImmutableList.copyOf(templog);
			return log1;
		}
		private ImmutableSet<Piece> updateRemaining(ImmutableSet<Piece> remain,Piece p){
			HashSet<Piece> remainingd = new HashSet<>(remain);
			if (remain.contains(mrX.piece())){
				detectives.forEach(detective -> remainingd.add(detective.piece()));
			}
			remainingd.remove(p);
			if (remainingd.isEmpty()){
				remainingd.add(mrX.piece());
			}
			HashSet<Piece> deadDetectives = new HashSet<>();
			for (Player d : detectives){
				if (makeSingleMoves(setup, detectives, d, d.location()).isEmpty())deadDetectives.add(d.piece());
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