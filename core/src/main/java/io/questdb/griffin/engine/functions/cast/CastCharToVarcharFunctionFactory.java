/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2023 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions.cast;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.functions.constants.VarcharConstant;
import io.questdb.std.IntList;
import io.questdb.std.ObjList;
import io.questdb.std.str.Utf16Sink;
import io.questdb.std.str.Utf8Sequence;
import io.questdb.std.str.Utf8Sink;
import io.questdb.std.str.Utf8StringSink;

public class CastCharToVarcharFunctionFactory implements FunctionFactory {

    @Override
    public String getSignature() {
        return "cast(Aø)";
    }

    @Override
    public Function newInstance(
            int position,
            ObjList<Function> args,
            IntList argPositions,
            CairoConfiguration configuration,
            SqlExecutionContext sqlExecutionContext
    ) {
        Function func = args.getQuick(0);
        if (func.isConstant()) {
            final char value = func.getChar(null);
            if (value == 0) {
                return VarcharConstant.NULL;
            }
            return new VarcharConstant(String.valueOf(value));
        }
        return new Func(func);
    }

    private static class Func extends AbstractCastToVarcharFunction {
        private final Utf8StringSink sinkA = new Utf8StringSink();
        private final Utf8StringSink sinkB = new Utf8StringSink();

        public Func(Function arg) {
            super(arg);
        }

        @Override
        public void getVarchar(Record rec, Utf8Sink utf8Sink) {
            final char value = arg.getChar(rec);
            if (value != 0) {
                utf8Sink.put(value);
            }
        }

        @Override
        public Utf8Sequence getVarcharA(Record rec) {
            final char value = arg.getChar(rec);
            if (value == 0) {
                return null;
            }
            sinkA.clear();
            sinkA.put(value);
            return sinkA;
        }

        @Override
        public Utf8Sequence getVarcharB(Record rec) {
            final char value = arg.getChar(rec);
            if (value == 0) {
                return null;
            }
            sinkB.clear();
            sinkB.put(value);
            return sinkB;
        }
    }
}
