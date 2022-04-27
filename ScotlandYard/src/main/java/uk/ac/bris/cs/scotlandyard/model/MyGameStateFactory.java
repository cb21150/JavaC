package uk.ac.bris.cs.scotlandyard.model;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

import com.google.common.collect.Iterables;
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
			HashSet<Piece> w = new HashSet<>();
			w = new HashSet<>(getPlayers()) ;
			w.remove(mrX.piece());
			if(log.size()==setup.moves.size() || (allstuck() && log.size()>0) || Startthrow()) {
				winner = ImmutableSet.of(mrX.piece());
				return winner;
				}
			else if(mrXcaptured() || mrXstuck()) {


				winner=ImmutableSet.copyOf(w);
				return winner;
			}

			winner = ImmutableSet.of();
			return winner;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			if(!winner.isEmpty()) return ImmutableSet.of();
			HashSet<SingleMove> moves1 = new HashSet<>();
			HashSet<Move> moves2 = new HashSet<>();
			HashSet<Move> fmoves = new HashSet<>();
			for (Player d  : detectives){
				if(remaining.contains(d.piece())){
					moves1 = (HashSet<SingleMove>) makeSingleMoves(setup, detectives, d, d.location());
					fmoves.addAll(moves1);
				}
			}
			if (remaining.contains(mrX.piece())) {
				moves2=(HashSet<Move>) makeDoubleMoves(setup, detectives, mrX, mrX.location());
				fmoves.addAll(moves2);

			}

			ImmutableSet<Move>Moves = ImmutableSet.copyOf(fmoves);
			return Moves;
		}

		@Override
		public GameState advance(Move move) {
			moves=getAvailableMoves();
			if(!moves.contains(move))throw new IllegalArgumentException("Illegal move "+ move);

			class moveType implements Visitor<GameState> {

				@Override
				public GameState visit(DoubleMove MoveD){
					ImmutableList<LogEntry> log1;
					log1 = updatelog(MoveD.ticket1,MoveD.destination1,log);
					mrX = mrX.at(MoveD.destination1);
					ImmutableList<LogEntry> log2 = updatelog(MoveD.ticket2, MoveD.destination2, log1);
					mrX = mrX.at(MoveD.destination2);
					mrX = mrX.use(MoveD.tickets());
					ImmutableSet Finalremaining = updateremaining(remaining, mrX.piece());
					return new MyGameState(setup, Finalremaining, log2, mrX, detectives);
				}

				@Override
				public GameState visit(SingleMove MoveS) {
					if(!getWinner().isEmpty() && log.size()==0)throw new IllegalArgumentException("game over");
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
							if (move.commencedBy().webColour() == d.piece().webColour()) {
								Player detective = d.use(MoveS.ticket);
								Player detective2 = detective.at(MoveS.destination);
								newDetectives.add(detective2);
								newDetectives.remove(d);
					            
								}
							}
						mrX=mrX.give(MoveS.ticket);
						detectives = newDetectives;
						return new MyGameState(setup, Finalremaining, log, mrX,detectives);
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
			this.winner = ImmutableSet.of();

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
			if (setup.moves.get(log2.size()) == true) {
				templog.add(LogEntry.reveal(t,d));
			}
			if (!setup.moves.get(log2.size())) {
				templog.add(LogEntry.hidden(t));
			}
			ImmutableList<LogEntry> log1 = ImmutableList.copyOf(templog);

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
		private boolean mrXcaptured(){
			for(Player d: detectives) {
				if(d.location()== mrX.location()){
					return true;
				}
			}
			return false;
		}
		private boolean allstuck() {
			HashSet<SingleMove> moves1;

			System.out.println(remaining);
			for (Player d : detectives) {
				System.out.println("det " + d.location());
				System.out.println("Mrx " + mrX.location());
				
				moves1 = (HashSet<SingleMove>) makeSingleMoves(setup, detectives, d, d.location());
				System.out.println(moves1);
				if(!moves1.isEmpty()){
					return false;
				}
			}
			return true;
		}
		private boolean mrXstuck(){
		Set<Move> Movemrx;
		Movemrx = makeDoubleMoves(setup, detectives, mrX, mrX.location());

		if(Movemrx.isEmpty()){
					return true ;
				}
			return false;
		}
		private boolean Startthrow()    {
			for(Player d: detectives)  {
				if(!Iterables.all(d.tickets().values(),(Integer e)  -> e ==0)){
					return false;
				}
			}
			return true;
		}

		}
	}