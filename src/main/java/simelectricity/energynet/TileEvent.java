package simelectricity.energynet;

import net.minecraft.world.level.block.entity.BlockEntity;
import simelectricity.api.tile.ISEGridTile;

public abstract class TileEvent extends EnergyEventBase {
    protected final BlockEntity te;

    private TileEvent(BlockEntity te) {
        this.te = te;
    }

    public static class Attach extends TileEvent {
        public Attach(BlockEntity te) {
            super(te);
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider, int pass) {
        	if (EnergyNetAgent.isNormalTile(this.te)) {
            	if (pass == TADD) {
            		dataProvider.addTile(te);
            	} else if (pass == TPARAMCHANGE) {
            		dataProvider.updateTileParam(te);
            	} else if (pass == TCONCHANGE) {
                    dataProvider.updateTileConnection(this.te);
            	}
        	}
        	
            if (this.te instanceof ISEGridTile && pass == TADD) {
                dataProvider.onGridTilePresent(this.te);
            }
        }

		@Override
		public boolean changedStructure() {
			return EnergyNetAgent.isNormalTile(this.te);
		}

		@Override
		public boolean needUpdate() {
			return EnergyNetAgent.isNormalTile(this.te);
		}
    }

    public static class Detach extends TileEvent {
        public Detach(BlockEntity te) {
            super(te);
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider, int pass) {
        	if (pass == TADD || pass == TDEL) {
        		if (EnergyNetAgent.isNormalTile(this.te)) {
                    dataProvider.removeTile(this.te);
            	}
        		

                if (this.te instanceof ISEGridTile && pass == TDEL) {
                    dataProvider.onGridTileInvalidate(this.te);
                }
            }

        }

		@Override
		public boolean changedStructure() {
			return EnergyNetAgent.isNormalTile(this.te);
		}

		@Override
		public boolean needUpdate() {
			return EnergyNetAgent.isNormalTile(this.te);
		}
    }

    public static class ParamChanged extends TileEvent {
        public ParamChanged(BlockEntity te) {
            super(te);
        }

		@Override
		public void process(EnergyNetDataProvider dataProvider, int pass) {
			if (pass == TPARAMCHANGE)
				dataProvider.updateTileParam(this.te);
		}

		@Override
		public boolean changedStructure() {
			return false;
		}

		@Override
		public boolean needUpdate() {
			return true;
		}
    }

    public static class ConnectionChanged extends TileEvent {
        public ConnectionChanged(BlockEntity te) {
            super(te);
        }

		@Override
		public void process(EnergyNetDataProvider dataProvider, int pass) {
			if (pass == TPARAMCHANGE)
				dataProvider.updateTileParam(this.te);
			if (pass == TCONCHANGE)
				dataProvider.updateTileConnection(this.te);
		}
        
		@Override
		public boolean changedStructure() {
			return true;
		}

		@Override
		public boolean needUpdate() {
			return true;
		}
    }
}
