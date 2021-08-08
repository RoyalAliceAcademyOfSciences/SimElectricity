package simelectricity.essential.client.grid;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rikka.librikka.DirHorizontal8;
import rikka.librikka.math.MathAssitant;
import rikka.librikka.math.Vec3f;

import java.util.LinkedList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PowerPoleRenderHelper {
    public final BlockPos pos;    //Real MC pos
    public final boolean mirroredAboutZ;
    public final int orientation;
    public final PowerPoleRenderHelper.Group[] groups;
    public final int insulatorPerGroup;
    
    /**
     * Buffer
     */
    public final LinkedList<PowerPoleRenderHelper.ConnectionInfo[]> connectionList = new LinkedList<>();
    public final LinkedList<PowerPoleRenderHelper.ExtraWireInfo> extraWireList = new LinkedList<>();
    
    public final List<BakedQuad> quadBuffer = new LinkedList<>();
    private boolean needBake = false;
    
    private int addedGroup;

    // Grid = 8-DirHorizontal8.ordinal() : east6 south4 west2 north0
    public PowerPoleRenderHelper(BlockPos pos, DirHorizontal8 dir8, int numOfGroup, int insulatorPerGroup) {
        this(pos, (8-dir8.ordinal()) & 7, false, numOfGroup, insulatorPerGroup);
    }
    
    public PowerPoleRenderHelper(BlockPos pos, int rotationMC, int numOfGroup, int insulatorPerGroup) {
        this(pos, rotationMC, false, numOfGroup, insulatorPerGroup);
    }

    public PowerPoleRenderHelper(BlockPos pos, Direction facing, boolean mirroredAboutZ, int numOfGroup, int insulatorPerGroup) {
        this(pos, PowerPoleRenderHelper.facing2rotation(facing), mirroredAboutZ, numOfGroup, insulatorPerGroup);
    }

    public PowerPoleRenderHelper(BlockPos pos, int rotationMC, boolean mirroredAboutZ, int numOfGroup, int insulatorPerGroup) {
        this.pos = pos;
        this.orientation = rotationMC;
        this.mirroredAboutZ = mirroredAboutZ;
        this.groups = new PowerPoleRenderHelper.Group[numOfGroup];
        this.insulatorPerGroup = insulatorPerGroup;
        this.addedGroup = 0;
    }

    public static int facing2rotation(Direction facing) {
        switch (facing) {
            case SOUTH:
                return 0;
            case WEST:
                return 6;
            case NORTH:
                return 4;
            case EAST:
                return 2;
            default:
                return 0;
        }
    }
    
    /**
     * @return true if interval1 and interval2 intersects
     */
    public static boolean hasIntersection(Vec3f from1, Vec3f to1, Vec3f from2, Vec3f to2) {
        float m1 = (from1.x - to1.x) / (from1.z - to1.z);
        float k1 = from1.x - from1.z * m1;
        float m2 = (from2.x - to2.x) / (from2.z - to2.z);
        float k2 = from2.x - from2.z * m2;

        float zc = (k2 - k1) / (m1 - m2);
        float zx = m1 * zc + k1;
        
        return from1.x > zx && zx > to1.x || from1.x < zx && zx < to1.x;
    }
    
/*    //http://blog.csdn.net/rickliuxiao/article/details/6259322
    private static boolean between(float a, float X0, float X1) {  
    	float temp1= a-X0;  
    	float temp2= a-X1;  
        return (temp1<1e-8 && temp2 > -1e-8) || (temp2 <1e-6 && temp1 > -1e-8); 
    } 
    
    *//**
     * @return true if interval(p1,p2) and interval(p3,p4) intersects
     *//*
    public static boolean hasIntersection(Vec3f p1, Vec3f p2, Vec3f p3, Vec3f p4) {  
    	float line_x,line_z; 
        if ((Mth.abs(p1.x-p2.x)<1e-6) && (Mth.abs(p3.x-p4.x)<1e-6)) {
            return false;
        } else if ((Mth.abs(p1.x-p2.x)<1e-6)) {  
            if (between(p1.x,p3.x,p4.x)) {  
            	float k = (p4.z-p3.z)/(p4.x-p3.x);  
                line_x = p1.x;  
                line_z = k*(line_x-p3.x)+p3.z;  
      
                return  between(line_z,p1.z,p2.z);
            }  
            else {  
                return false;  
            }  
        }  
        else if (Mth.abs(p3.x-p4.x) < 1e-6) {  
            if (between(p3.x,p1.x,p2.x)) {  
            	float k = (p2.z-p1.z)/(p2.x-p1.x);  
                line_x = p3.x;  
                line_z = k*(line_x-p2.x)+p2.z;  
      
                return between(line_z,p3.z,p4.z);
            }  
            else {
                return false;  
            }
        } else {  
            float k1 = (p2.z-p1.z)/(p2.x-p1.x);   
            float k2 = (p4.z-p3.z)/(p4.x-p3.x);  
      
            if (Mth.abs(k1-k2) < 1e-6) {  
                return false;  
            } else {  
                line_x = ((p3.z - p1.z) - (k2*p3.x - k1*p1.x)) / (k1-k2);  
                line_z = k1*(line_x-p1.x)+p1.z;  
            }  
      
            return between(line_x,p1.x,p2.x)&&between(line_x,p3.x,p4.x); 
        }  
    }  */

    private static float calcInitSlope(float length, float tension) {
        double b = 4 * tension / length;
        double a = -b / length;
        return (float) -Math.atan(2 * a + b);
    }

    private static float calcAngle(float distance, float yFrom, float yTo, float tension) {
        return calcInitSlope(distance, tension) + (float) Math.atan((yTo - yFrom) / distance);
    }

    private static Vec3f fixConnectionPoints(Vec3f from, Vec3f to, float distance, float angle, float insulatorLength, float tension) {
        float lcos = insulatorLength * Mth.cos(angle);
        float atan = (float) Math.atan2(to.x - from.x, from.z - to.z);

        return from.add(lcos * Mth.sin(atan), insulatorLength * Mth.sin(angle), -lcos * Mth.cos(atan));
    }

    ////////////////////////////
    /// Utils
    ////////////////////////////
    public static void notifyChanged(ISEPowerPole... list) {
        GridRenderMonitor.instance.notifyChanged(list);
    }
   
    public final PowerPoleRenderHelper.Insulator createInsulator(float length, float tension, float offsetX, float offsetY, float offsetZ) {
        if (this.mirroredAboutZ)
            offsetX = -offsetX;
        
        float cos = MathAssitant.cosAngle(this.orientation * 45);
        float sin = MathAssitant.sinAngle(this.orientation * 45);
        
        float rotatedX = offsetX * cos + offsetZ * sin;
        float rotatedZ = -offsetX* sin + offsetZ * cos;
        
        return new PowerPoleRenderHelper.Insulator(this, length, tension, rotatedX + 0.5F, offsetY, rotatedZ + 0.5F);
    }

    public final void addInsulatorGroup(float centerX, float centerY, float centerZ, PowerPoleRenderHelper.Insulator... insulators) {
        if (addedGroup == groups.length)
            return;
        
        if (insulators.length != insulatorPerGroup)
            return;
        
        float cos = MathAssitant.cosAngle(this.orientation * 45);
        float sin = MathAssitant.sinAngle(this.orientation * 45);
        
        float rotatedX = centerX * cos + centerZ * sin;
        float rotatedZ = -centerX* sin + centerZ * cos;
        
        groups[addedGroup] = new PowerPoleRenderHelper.Group(this, rotatedX + 0.5F, centerY, rotatedZ + 0.5F, insulators);
        addedGroup++;
    }

    public final boolean needBake() {
    	if (needBake) {
    		needBake = false;
    		return true;
    	}
    	return false;
    }
    
    public final void updateRenderData(BlockGetter world, BlockPos... neighborPosList) {
        this.connectionList.clear();
        this.extraWireList.clear();
        addNeighors(world, neighborPosList);
    }
    
    public final void postUpdate() {
        onUpdate();
        
        //Bake Quads
        this.quadBuffer.clear();
        this.needBake = true;
    }
    
    /**
     * Override this to add extra wires
     * @return true for success operation
     */
    protected void onUpdate() {}
    
    protected BlockPos getAccessoryPos() {return null;}

    private final void addNeighors(BlockGetter world, BlockPos... neighborPosList) {
        for (BlockPos neighborPos : neighborPosList) {
            if (neighborPos == null)
                continue;

            BlockEntity te = world.getBlockEntity(neighborPos);
            if (te instanceof ISEPowerPole) {
                PowerPoleRenderHelper helper = ((ISEPowerPole) te).getRenderHelper();
                if (helper == null) {
                	findVirtualConnection(neighborPos);
                }else {
                	findConnection(helper);
                }
            }else {
            	findVirtualConnection(neighborPos);
            }
        }
    }
    
    public final void addExtraWire(Vec3f from, Vec3f to, float tension) {
        this.extraWireList.add(new PowerPoleRenderHelper.ExtraWireInfo(from, to, tension, false));
    }
    
    public final void addExtraWire(Vec3f from, Vec3f to, float tension, boolean useCatenary) {
        this.extraWireList.add(new PowerPoleRenderHelper.ExtraWireInfo(from, to, tension, useCatenary));
    }

    private void findVirtualConnection(BlockPos neighborCoord) {
        PowerPoleRenderHelper.Group group1 = null;
        float minDistance = Float.MAX_VALUE;

        //Find shortest path
        for (int i = 0; i < this.groups.length; i++) {
            float distance = this.groups[i].distanceTo(neighborCoord);
            if (distance < minDistance) {
                minDistance = distance;
                group1 = this.groups[i];
            }
        }
        
        PowerPoleRenderHelper.ConnectionInfo[] ret = new PowerPoleRenderHelper.ConnectionInfo[this.insulatorPerGroup];
        Vec3f from1 = group1.insulators[0].realPos;
        Vec3f to1 = group1.insulators[0].virtualize(neighborCoord);
        Vec3f from2 = group1.insulators[insulatorPerGroup - 1].realPos;
        Vec3f to2 = group1.insulators[insulatorPerGroup - 1].virtualize(neighborCoord);
        if (hasIntersection(from1, to1, from2, to2)) {
        	for (int i = 0; i < insulatorPerGroup; i++) {
        		ret[i] = new PowerPoleRenderHelper.ConnectionInfo(group1.insulators[i], group1.insulators[insulatorPerGroup - 1 - i].virtualize(neighborCoord));
        	}
        } else {
            for (int i = 0; i < insulatorPerGroup; i++) {
                ret[i] = new PowerPoleRenderHelper.ConnectionInfo(group1.insulators[i], group1.insulators[i].virtualize(neighborCoord));
            }
        }
        
        this.connectionList.add(ret);        
    }
    
    private void findConnection(PowerPoleRenderHelper neighbor) {
        PowerPoleRenderHelper.Group group1 = null;
        PowerPoleRenderHelper.Group group2 = null;
        float minDistance = Float.MAX_VALUE;

        //Find shortest path
        for (int i = 0; i < this.groups.length; i++) {
            for (int j = 0; j < neighbor.groups.length; j++) {
                float distance = this.groups[i].distanceTo(neighbor.groups[j]);
                if (distance < minDistance) {
                    minDistance = distance;
                    group1 = this.groups[i];
                    group2 = neighbor.groups[j];
                }
            }
        }

        PowerPoleRenderHelper.ConnectionInfo[] ret = new PowerPoleRenderHelper.ConnectionInfo[this.insulatorPerGroup];

        Vec3f from1 = group1.insulators[0].realPos;
        Vec3f to1 = group2.insulators[0].realPos;
        Vec3f from2 = group1.insulators[insulatorPerGroup - 1].realPos;
        Vec3f to2 = group2.insulators[insulatorPerGroup - 1].realPos;
        if (hasIntersection(from1, to1, from2, to2)) {
            for (int i = 0; i < insulatorPerGroup; i++) {
                ret[i] = new PowerPoleRenderHelper.ConnectionInfo(group1.insulators[i], group2.insulators[insulatorPerGroup - 1 - i]);
            }
        }else {
            for (int i = 0; i < insulatorPerGroup; i++) {
                ret[i] = new PowerPoleRenderHelper.ConnectionInfo(group1.insulators[i], group2.insulators[i]);
            }
        }

        this.connectionList.add(ret);
    }

    public static class Insulator {
        public final PowerPoleRenderHelper parent;
        /**
         * Rotated offsets
         */
        public final float length, tension, offsetX, offsetY, offsetZ;
        public final Vec3f realPos;
		private Group group;

        private Insulator(PowerPoleRenderHelper parent, float length, float tension, float offsetX, float offsetY, float offsetZ) {
            this.parent = parent;
            this.length = length;
            this.tension = tension;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            realPos = new Vec3f(offsetX + parent.pos.getX(), offsetY + parent.pos.getY(), offsetZ + parent.pos.getZ());
        }
        
        public Group getGroup() {
        	return group;
        }
        
        public Vec3f virtualize(BlockPos newPos) {
        	return realPos.add(newPos.subtract(parent.pos));
        }
    }

    public static class Group {
        public final PowerPoleRenderHelper parent;
        /**
         * Center offsets, XZ respect to Block center, rotated
         */
        public final float centerX, centerY, centerZ;
        public final PowerPoleRenderHelper.Insulator[] insulators;

        private Group(PowerPoleRenderHelper parent, float centerX, float centerY, float centerZ, PowerPoleRenderHelper.Insulator... insulators) {
            this.parent = parent;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.insulators = new PowerPoleRenderHelper.Insulator[insulators.length];
            for (int i = 0; i < insulators.length; i++) {
                this.insulators[i] = insulators[i];
                this.insulators[i].group = this;
            }
        }
        
        /**
         * Find the real distance
         * @param group
         * @return
         */
        public float distanceTo(PowerPoleRenderHelper.Group group) {
            //Normalize respect to current BlockEntity coordinate
            Vec3i offset = group.parent.pos.subtract(this.parent.pos);

            float x = offset.getX() + group.centerX - this.centerX;
            float z = offset.getZ() + group.centerZ - this.centerZ;

            return Mth.sqrt(x * x + z * z);
        }
        
        /**
         * Estimate the distance
         * @param pos
         * @return
         */
        public float distanceTo(Vec3i pos) {
            float x = pos.getX() - (this.parent.pos.getX() + this.centerX);
            float z = pos.getZ() - (this.parent.pos.getZ() + this.centerZ);

            return Mth.sqrt(x * x + z * z);
        }
        
        public Group closest(Group... groups) {
        	Group ret = null;
        	float minDistance = Float.MAX_VALUE;
        	
        	for (Group group: groups) {
                float distance = distanceTo(group);
                if (distance < minDistance) {
                    minDistance = distance;
                    ret = group;
                }
        	}
        	
        	return ret;
        }
        
        public ConnectionInfo[] closest(ConnectionInfo[]... connections) {
        	ConnectionInfo[] ret = null;
        	float minDistance = Float.MAX_VALUE;
        	
        	for (ConnectionInfo[] connection: connections) {
                float distance = distanceTo(connection[0].fromGroup);
                if (distance < minDistance) {
                    minDistance = distance;
                    ret = connection;
                }
        	}
        	
        	return ret;
        }
    }
    
    public static class ConnectionInfo {
        public final Vec3f from, to;
        public final Vec3f fixedFrom, fixedTo;
        public final Vec3f initalSlopVec;	//Unit Length
        public final float insulatorAngle, tension;
        public final Group fromGroup;

        public final boolean isVirtual;
        
        private ConnectionInfo(PowerPoleRenderHelper.Insulator from, PowerPoleRenderHelper.Insulator to) {
            float tension = from.tension;
            float tension2 = to.tension;
            tension = tension < tension2 ? tension : tension2;

            this.from = from.realPos;
            this.to = to.realPos;

            float distance = this.from.distanceTo(this.to);
            float angle = PowerPoleRenderHelper.calcAngle(distance, this.from.y, this.to.y, tension);
            this.initalSlopVec = PowerPoleRenderHelper.fixConnectionPoints(this.from, this.to, distance, angle, 1, tension).add(-this.from.x, -this.from.y, -this.from.z);
            Vec3f fixedFrom = PowerPoleRenderHelper.fixConnectionPoints(this.from, this.to, distance, angle, from.length, tension);

            float dummyAngle = PowerPoleRenderHelper.calcAngle(distance, this.to.y, this.from.y, tension);
            Vec3f fixedTo = PowerPoleRenderHelper.fixConnectionPoints(this.to, this.from, distance, dummyAngle, to.length, tension);

            this.fixedFrom = fixedFrom;
            this.fixedTo = fixedTo;
            this.insulatorAngle = angle;
            this.tension = tension;
            this.fromGroup = from.group;
            
            this.isVirtual = false;
        }
        
        private ConnectionInfo(PowerPoleRenderHelper.Insulator from, Vec3f to) {
        	float tension = from.tension;
        	
        	this.from = from.realPos;
        	this.to = to;
        	
            float distance = this.from.distanceTo(this.to);
            float angle = PowerPoleRenderHelper.calcAngle(distance, this.from.y, this.to.y, tension);
            this.initalSlopVec = PowerPoleRenderHelper.fixConnectionPoints(this.from, this.to, distance, angle, 1, tension).add(-this.from.x, -this.from.y, -this.from.z);
            Vec3f fixedFrom = PowerPoleRenderHelper.fixConnectionPoints(this.from, this.to, distance, angle, from.length, tension);

            float dummyAngle = PowerPoleRenderHelper.calcAngle(distance, this.to.y, this.from.y, tension);
            Vec3f fixedTo = PowerPoleRenderHelper.fixConnectionPoints(this.to, this.from, distance, dummyAngle, from.length, tension);
            
            this.fixedFrom = fixedFrom;
            this.fixedTo = fixedTo;
            this.insulatorAngle = angle;
            this.tension = tension;
            this.fromGroup = from.group;
            
            this.isVirtual = true;
        }
        
        public float calcAngleFromXInDegree() {
        	return this.fixedFrom.calcAngleFromXInDegree(this.fixedTo);
        }
        
        public Vec3f pointOnCable(float dist) {
/*        	float x = fixedFrom.x-from.x;
        	float y = fixedFrom.y-from.y;
        	float z = fixedFrom.z-from.z;*/
        	float x = this.initalSlopVec.x;
        	float y = this.initalSlopVec.y;
        	float z = this.initalSlopVec.z;
        	
        	return new Vec3f(x*dist + fixedFrom.x, y*dist + fixedFrom.y, z*dist + fixedFrom.z);
        }
    }

    public static class ExtraWireInfo {
        public final Vec3f from, to;
        public final float tension;
        public final boolean useCatenary;

        private ExtraWireInfo(Vec3f from, Vec3f to, float tension, boolean useCatenary) {
            this.from = from;
            this.to = to;
            this.tension = tension;
            this.useCatenary = useCatenary;
        }
    }
}
