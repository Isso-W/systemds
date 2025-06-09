package org.apache.sysds.runtime.transform.decode;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.sysds.common.Types.ValueType;
import org.apache.sysds.runtime.DMLRuntimeException;
import org.apache.sysds.runtime.frame.data.FrameBlock;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.util.UtilFunctions;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ColumnDecoderBin extends ColumnDecoder {
    private static final long serialVersionUID = -3784249774608228805L;

    private int[] _numBins;
    private double[][] _binMins = null;
    private double[][] _binMaxs = null;

    public ColumnDecoderBin() {
        super(null, null);
    }

    protected ColumnDecoderBin(ValueType[] schema, int[] binCols) {
        super(schema, binCols);
    }


    @Override
    public FrameBlock columnDecode(MatrixBlock in, FrameBlock out) {
        //TODO: 把DecoderBin那边的方法毛过来，传入的参数中，in.ColumnBlock.data是需要decode的数据
        // in.ColumnBlock.targetCols里面有每列在原来Block中的位置，位置最后写回的时候调用out.set(r, targetCols[xxx], value);就可以
        return null;
    }

    @Override
    public void columnDecode(MatrixBlock in, FrameBlock out, int rl, int ru) {
        //TODO: 同上
    }

    @Override
    public ColumnDecoder subRangeDecoder(int colStart, int colEnd, int dummycodedOffset) {
        // federated not supported yet
        throw new NotImplementedException();
    }

    @Override
    public void initMetaData(FrameBlock meta) {
        //initialize bin boundaries
        _numBins = new int[_colList.length];
        _binMins = new double[_colList.length][];
        _binMaxs = new double[_colList.length][];

        //parse and insert bin boundaries
        for( int j=0; j<_colList.length; j++ ) {
            int numBins = (int)meta.getColumnMetadata(_colList[j]-1).getNumDistinct();
            _binMins[j] = new double[numBins];
            _binMaxs[j] = new double[numBins];
            for( int i=0; i<meta.getNumRows() & i<numBins; i++ ) {
                if( meta.get(i, _colList[j]-1)==null  ) {
                    if( i+1 < numBins )
                        throw new DMLRuntimeException("Did not reach number of bins: "+(i+1)+"/"+numBins);
                    break; //reached end of bins
                }
                String[] parts = UtilFunctions.splitRecodeEntry(
                        meta.get(i, _colList[j]-1).toString());
                _binMins[j][i] = Double.parseDouble(parts[0]);
                _binMaxs[j][i] = Double.parseDouble(parts[1]);
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        for( int i=0; i<_colList.length; i++ ) {
            int len = _numBins[i];
            out.writeInt(len);
            for(int j=0; j<len; j++) {
                out.writeDouble(_binMins[i][j]);
                out.writeDouble(_binMaxs[i][j]);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        super.readExternal(in);
        _numBins = new int[_colList.length];
        _binMins = new double[_colList.length][];
        _binMaxs = new double[_colList.length][];
        for( int i=0; i<_colList.length; i++ ) {
            int len = in.readInt();
            _numBins[i] = len;
            for(int j=0; j<len; j++) {
                _binMins[i][j] = in.readDouble();
                _binMaxs[i][j] = in.readDouble();
            }
        }
    }
}
