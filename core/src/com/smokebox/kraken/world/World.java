package com.smokebox.kraken.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.smokebox.kraken.Game;
import com.smokebox.kraken.ability.CollidesWithWorld;
import com.smokebox.kraken.weaponry.ammunition.Projectile;
import com.smokebox.lib.pcg.dungeon.RoomSpreadDungeon;
import com.smokebox.lib.pcg.dungeon.RoomsWithTree;
import com.smokebox.lib.utils.Intersect;
import com.smokebox.lib.utils.MathUtils;
import com.smokebox.lib.utils.Vector2;
import com.smokebox.lib.utils.geom.HalfSpace;
import com.smokebox.lib.utils.geom.Line;
import com.smokebox.lib.utils.geom.Rectangle;
import com.smokebox.lib.utils.geom.UnifiablePolyedge;
import com.smokebox.lib.utils.pathfinding.Connection;
import com.smokebox.lib.utils.pathfinding.Euclidian;
import com.smokebox.lib.utils.pathfinding.PathfindAStar;
import com.smokebox.lib.utils.pathfinding.StarNode;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Harald Floor Wilhelmsen
 *
 */
public class World {
	
	private ArrayList<HalfSpace> walls = new ArrayList<HalfSpace>();
	
	private PathfindAStar pathfinder;
	
	Game game;
	
	
	public World(int size, long seed, Game game) {
		this.game = game;
		
		pathfinder = new PathfindAStar();
		if(seed == 0) seed = (long)Math.round(Math.random()*10000);
		RoomsWithTree dungeon = RoomSpreadDungeon.RoomSpreadFloor(size, 5, 5, new Random(seed));
		
		int[][] asInt = RoomSpreadDungeon.asInt2(dungeon);
		
		ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
		for(int i = 0; i < asInt.length; i++) {
			for(int j = 0; j < asInt[0].length; j++) {
				if(asInt[i][j] > 0) rects.add(new Rectangle(i, j, 1, 1));
			}
		}
		
		ArrayList<Line> lines = new ArrayList<Line>();
		for(Rectangle r : rects) {
			lines.add(new Line(
					r.x, 			r.y, 
					r.x, 			r.y + r.height));
			lines.add(new Line(
					r.x + r.width, 	r.y, 
					r.x + r.width, 	r.y + r.height));
			lines.add(new Line(
					r.x, 			r.y, 
					r.x + r.width, 	r.y));
			lines.add(new Line(
					r.x, 			r.y + r.height, 
					r.x + r.width,	r.y + r.height));
		}
		
		UnifiablePolyedge p = new UnifiablePolyedge(lines);
		pathfinder.defineWorldFromPolyedge(p, dungeon);
		lines = p.getEdges();
		
		for(Line l : lines) {
			Vector2 v = new Vector2(l.x2 - l.x, l.y2 - l.y);
			Vector2 perp1 = new Vector2(-v.y, v.x).nor();
			Vector2 perp2 = new Vector2(v.y, -v.x).nor();
			Vector2 midPos = new Vector2(l.x + (l.x2 - l.x)/2, l.y + (l.y2 - l.y)/2);
			walls.add(new HalfSpace(l, Intersect.pointInsidePolyedge(midPos.x + perp1.x * 0.01f, midPos.y + perp1.y * 0.01f, lines, -10000) ? perp1.nor() : perp2.nor()));
		}
	}
	
	public void drawWallDirections(ShapeRenderer sr) {
		for(HalfSpace w : walls) {
			Line l = w.line;
			Vector2 midPos = new Vector2(l.x + (l.x2 - l.x)/2, l.y + (l.y2 - l.y)/2);
			sr.line(midPos.x, midPos.y, midPos.x + w.inside.x, midPos.y + w.inside.y);
		}
	}
	
	public void drawPathGraph(ShapeRenderer sr) {
		ArrayList<StarNode> stars = pathfinder.getNodes();
		for(StarNode s : stars) {
			sr.setColor(0, 0.4f, 0.4f, 1);
			for(Connection c : s.getConnections()) {
				sr.line(c.start.x, c.start.y, c.end.x, c.end.y);
				sr.circle(c.start.x + (c.end.x - c.start.x)*0.5f, c.start.y + (c.end.y - c.start.y)*0.5f, 0.1f, 20);
			}
			sr.setColor(0, 0.8f, 0.8f, 1);
			sr.circle(s.x, s.y, 0.2f, 4);
		}
	}
	
	public void drawFancyWalls(ShapeRenderer sr, Vector2 cam, float cameraHeight) {
		for(HalfSpace w : walls) {
			Line l = w.line;
			if(game.isOnScreen(l.x, l.y) || game.isOnScreen(l.x2, l.y2)) {
				Vector2 wDist = new Vector2();
				float angle;
				float r;
				float s = 1/cameraHeight;
				
				wDist.set(l.x - cam.x, l.y - cam.y);
				r = wDist.getMag();
				angle = wDist.getAngleAsRadians();
				Vector2 wc1 = new Vector2(	l.x		+ s * r * (float)Math.cos(angle), 
											l.y 	+ s * r * (float)Math.sin(angle)
											);
				
				wDist.set(l.x2 - cam.x, l.y2 - cam.y);
				r = wDist.getMag();
				angle = wDist.getAngleAsRadians();
				Vector2 wc2 = new Vector2(	l.x2	+ s * r * (float)Math.cos(angle), 
											l.y2	+ s * r * (float)Math.sin(angle)
											);
				
				float colorIncScl = 1;
				float[] c = MathUtils.HSLtoRGB(l.y * colorIncScl, 1, 1);
				float[] c2 = MathUtils.HSLtoRGB(l.y2*colorIncScl, 1, 1);
				sr.line(l.x, l.y, l.x2, l.y2, new Color(c[0], c[1], c[2], c[3]), new Color(c2[0], c2[1], c2[2], c2[3]));
				
				c = MathUtils.HSLtoRGB(l.y*colorIncScl, 1, 1);
				c2 = MathUtils.HSLtoRGB(wc1.y*colorIncScl, 1, 1);
				sr.line(l.x, l.y, wc1.x, wc1.y, new Color(c[0], c[1], c[2], c[3]), new Color(c2[0], c2[1], c2[2], c2[3]));
				
				c = MathUtils.HSLtoRGB(l.y2*colorIncScl, 1, 1);
				c2 = MathUtils.HSLtoRGB(wc2.y*colorIncScl, 1, 1);
				sr.line(l.x2, l.y2, wc2.x, wc2.y, new Color(c[0], c[1], c[2], c[3]), new Color(c2[0], c2[1], c2[2], c2[3]));
				
				c = MathUtils.HSLtoRGB(wc1.y*colorIncScl, 1, 1);
				c2 = MathUtils.HSLtoRGB(wc2.y*colorIncScl, 1, 1);
				sr.line(wc1.x, wc1.y, wc2.x, wc2.y, new Color(c[0], c[1], c[2], c[3]), new Color(c2[0], c2[1], c2[2], c2[3]));
			}
		}
	}
	
	/**
	 * Pathplanning
	 * Uses and implementation of the A*-algorithm to find a path on the
	 * previously generated graph from coordinates "start" to "end"
	 * @param start	Coordinated to start from
	 * @param end	Coordinates to end on
	 * @return	A list of points the character must go through
	 */
	public ArrayList<Vector2> getPath(Vector2 start, Vector2 end) {
		ArrayList<Vector2> path = new ArrayList<Vector2>();
		path.add(start);
		StarNode endNode = pathfinder.getNodeClosestTo(end);
		ArrayList<StarNode> nodePath = pathfinder.findPath(pathfinder.getNodeClosestTo(start), endNode, new Euclidian(new Vector2(end.x, end.y)));
		if(nodePath != null) {
			for(StarNode i : nodePath) {
				path.add(new Vector2(i.x, i.y));
			}
		}
		return path;
	}
	
	/**
	 * Returns a boolean if the coordinates given can
	 * be connected with no intersections with the map
	 * @param start	Coordinates to start from
	 * @param end	Coordinates to end at
	 * @param lines	Linelist of intersectors
	 * @return	Boolean, true for no intersection
	 */
	public boolean canSee(Vector2 start, Vector2 end, ArrayList<Line> lines) {
		Line l = new Line(start, end);
		for(Line i : lines) {
			if(Intersect.intersection(l, i)) {
				return false;
			}
		}
		return true;
	}
	
	public void handleProjectileWithWalls(Projectile p) {
		boolean done = false;
		for(HalfSpace w : walls) {
			Vector2 pen = Intersect.distance(w.line, p.getPos());
			float pd = pen.getMag();
			float r = p.getBoundingRadius();
			if(pd < r) {
				// Intersection
				// Move collides-object in walls-normalvector's direction by pd
				p.handleWallIntersection(new Vector2(w.inside.nor()).add(new Vector2(pen).flip()).nor().scl(p.getBoundingRadius() - pd));
				done = true;
			}
		}
		if(done) return;
		if(!Intersect.pointInsideWallList(p.getPos().x, p.getPos().y, walls, -10000)) {
			HalfSpace w = getClosestWall(walls, p.getPos());
			p.handleOutsideWalls(new Vector2(w.inside));
		}
	}
	
	public void calulateIntersectionForces(ArrayList<CollidesWithWorld> cases) {
		/* 1. Iterate over all walls
		 * 	Check intersection with all objects
		 * 	If an object intersects with wall in question:
		 * 		True: 
		 * 			Use the intersecting wall(s) to generate compensating force
		 * 			Remove object from list
		 * 		False:
		 * 			Continue to next step
		 * 2. Check if object is inside map
		 * 		True:
		 * 			Don't do anything to the object and remove from list
		 * 		False:
		 * 			Find nearest wall
		 * 			Add force proportional to penetrating distance
		 */
		
		for(int i = 0; i < cases.size();) {
			boolean done = false;
			CollidesWithWorld c = cases.get(i);
			// If object is not scheduled for removal
			if(!c.isAlive()) {
				// Remove object from collides-list
				cases.remove(i);
				continue;
			} else {
				i++;
				for(HalfSpace w : walls) {
					Vector2 p = Intersect.distance(w.line, c.getPos());
					float pd = p.getMag();
					float r = c.getBoundingRadius();
					if(pd < r) {
						// Intersection
						// Move collides-object in walls-normalvector's direction by pd
						c.handleWallIntersection(new Vector2(w.inside.nor()).add(new Vector2(p).flip()).nor().scl(c.getBoundingRadius() - pd));
						done = true;
					}
				}
				if(done) continue;
				if(!Intersect.pointInsideWallList(c.getPos().x, c.getPos().y, walls, -10000)) {
					HalfSpace w = getClosestWall(walls, c.getPos());
					c.handleOutsideWalls(new Vector2(w.inside));
				}
			}
		}
		
	}
	
	public HalfSpace getClosestWall(ArrayList<HalfSpace> walls, Vector2 p) {
		HalfSpace closestWall = walls.get(0);
		float length = closestWall.line.getMinimumDistance(p).getMag2();
		for(HalfSpace w : walls) {
			Vector2 closesDistInQuestion = w.line.getMinimumDistance(p);
			float lengthInQuestion = closesDistInQuestion.getMag2();
			if(lengthInQuestion < length) {
				length = lengthInQuestion;
				closestWall = w;
			}
		}
		return closestWall;
	}
	
	public ArrayList<HalfSpace> getWalls() {
		return walls;
	}
}