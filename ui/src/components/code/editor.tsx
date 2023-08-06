import React, {useEffect, useState} from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { historyField } from '@codemirror/commands';
import { javascript } from '@codemirror/lang-javascript';
import { materialDark, materialDarkInit, materialLight, materialLightInit } from '@uiw/codemirror-theme-material';
import Controls from "@/components/code/controls";
const stateFields = { history: historyField };
import { createTheme } from '@uiw/codemirror-themes';
import { tags as t } from '@lezer/highlight';

const myTheme = createTheme({
  theme: 'light',
  settings: {
    background: '#efefef',
    foreground: '#75baff',
    caret: '#5d00ff',
    selection: '#036dd626',
    selectionMatch: '#036dd626',
    lineHighlight: '#8a91991a',
    gutterBackground: '#efefef',
    gutterForeground: '#8a919966',
  },
  styles: [
    { tag: [t.standard(t.tagName), t.tagName], color: '#116329' },
    { tag: [t.comment, t.bracket], color: '#6a737d' },
    { tag: [t.className, t.propertyName], color: '#6f42c1' },
    { tag: [t.variableName, t.attributeName, t.number, t.operator], color: '#005cc5' },
    { tag: [t.keyword, t.typeName, t.typeOperator, t.typeName], color: '#d73a49' },
    { tag: [t.string, t.meta, t.regexp], color: '#032f62' },
    { tag: [t.name, t.quote], color: '#22863a' },
    { tag: [t.heading], color: '#24292e', fontWeight: 'bold' },
    { tag: [t.emphasis], color: '#24292e', fontStyle: 'italic' },
    { tag: [t.deleted], color: '#b31d28', backgroundColor: 'ffeef0' },
    { tag: [t.atom, t.bool, t.special(t.variableName)], color: '#e36209' },
    { tag: [t.url, t.escape, t.regexp, t.link], color: '#032f62' },
    { tag: t.link, textDecoration: 'underline' },
    { tag: t.strikethrough, textDecoration: 'line-through' },
    { tag: t.invalid, color: '#cb2431' },
  ],
});

function CodeEditor() {
  const [serializedState, setSerializedState] = useState<string | null>(null);
  const [value, setValue] = useState<string>();

  useEffect(() => {
    setSerializedState(localStorage.getItem('editorState'))
    setValue(localStorage.getItem('codeValue') || '/* Game of Life\n' +
        ' * Implemented in TypeScript\n' +
        ' * To learn more about TypeScript, please visit http://www.typescriptlang.org/\n' +
        ' */\n' +
        '\n' +
        'namespace Conway {\n' +
        '\n' +
        '  export class Cell {\n' +
        '    public row: number;\n' +
        '    public col: number;\n' +
        '    public live: boolean;\n' +
        '\n' +
        '    constructor(row: number, col: number, live: boolean) {\n' +
        '      this.row = row;\n' +
        '      this.col = col;\n' +
        '      this.live = live;\n' +
        '    }\n' +
        '  }\n' +
        '\n' +
        '  export class GameOfLife {\n' +
        '    private gridSize: number;\n' +
        '    private canvasSize: number;\n' +
        '    private lineColor: string;\n' +
        '    private liveColor: string;\n' +
        '    private deadColor: string;\n' +
        '    private initialLifeProbability: number;\n' +
        '    private animationRate: number;\n' +
        '    private cellSize: number;\n' +
        '    private context: CanvasRenderingContext2D;\n' +
        '    private world;\n' +
        '\n' +
        '\n' +
        '    constructor() {\n' +
        '      this.gridSize = 50;\n' +
        '      this.canvasSize = 600;\n' +
        '      this.lineColor = \'#cdcdcd\';\n' +
        '      this.liveColor = \'#666\';\n' +
        '      this.deadColor = \'#eee\';\n' +
        '      this.initialLifeProbability = 0.5;\n' +
        '      this.animationRate = 60;\n' +
        '      this.cellSize = 0;\n' +
        '      this.world = this.createWorld();\n' +
        '      this.circleOfLife();\n' +
        '    }\n' +
        '\n' +
        '    public createWorld() {\n' +
        '      return this.travelWorld( (cell : Cell) =>  {\n' +
        '        cell.live = Math.random() < this.initialLifeProbability;\n' +
        '        return cell;\n' +
        '      });\n' +
        '    }\n' +
        '\n' +
        '    public circleOfLife() : void {\n' +
        '      this.world = this.travelWorld( (cell: Cell) => {\n' +
        '        cell = this.world[cell.row][cell.col];\n' +
        '        this.draw(cell);\n' +
        '        return this.resolveNextGeneration(cell);\n' +
        '      });\n' +
        '      setTimeout( () => {this.circleOfLife()}, this.animationRate);\n' +
        '    }\n' +
        '\n' +
        '    public resolveNextGeneration(cell : Cell) {\n' +
        '      var count = this.countNeighbors(cell);\n' +
        '      var newCell = new Cell(cell.row, cell.col, cell.live);\n' +
        '      if(count < 2 || count > 3) newCell.live = false;\n' +
        '      else if(count == 3) newCell.live = true;\n' +
        '      return newCell;\n' +
        '    }\n' +
        '\n' +
        '    public countNeighbors(cell : Cell) {\n' +
        '      var neighbors = 0;\n' +
        '      for(var row = -1; row <=1; row++) {\n' +
        '        for(var col = -1; col <= 1; col++) {\n' +
        '          if(row == 0 && col == 0) continue;\n' +
        '          if(this.isAlive(cell.row + row, cell.col + col)) {\n' +
        '            neighbors++;\n' +
        '          }\n' +
        '        }\n' +
        '      }\n' +
        '      return neighbors;\n' +
        '    }\n' +
        '\n' +
        '    public isAlive(row : number, col : number) {\n' +
        '      if(row < 0 || col < 0 || row >= this.gridSize || col >= this.gridSize) return false;\n' +
        '      return this.world[row][col].live;\n' +
        '    }\n' +
        '\n' +
        '    public travelWorld(callback) {\n' +
        '      var result = [];\n' +
        '      for(var row = 0; row < this.gridSize; row++) {\n' +
        '        var rowData = [];\n' +
        '        for(var col = 0; col < this.gridSize; col++) {\n' +
        '          rowData.push(callback(new Cell(row, col, false)));\n' +
        '        }\n' +
        '        result.push(rowData);\n' +
        '      }\n' +
        '      return result;\n' +
        '    }\n' +
        '\n' +
        '    public draw(cell : Cell) {\n' +
        '      if(this.context == null) this.context = this.createDrawingContext();\n' +
        '      if(this.cellSize == 0) this.cellSize = this.canvasSize/this.gridSize;\n' +
        '\n' +
        '      this.context.strokeStyle = this.lineColor;\n' +
        '      this.context.strokeRect(cell.row * this.cellSize, cell.col*this.cellSize, this.cellSize, this.cellSize);\n' +
        '      this.context.fillStyle = cell.live ? this.liveColor : this.deadColor;\n' +
        '      this.context.fillRect(cell.row * this.cellSize, cell.col*this.cellSize, this.cellSize, this.cellSize);\n' +
        '    }\n' +
        '\n' +
        '    public createDrawingContext() {\n' +
        '      var canvas = <HTMLCanvasElement> document.getElementById(\'conway-canvas\');\n' +
        '      if(canvas == null) {\n' +
        '          canvas = document.createElement(\'canvas\');\n' +
        '          canvas.id = \'conway-canvas\';\n' +
        '          canvas.width = this.canvasSize;\n' +
        '          canvas.height = this.canvasSize;\n' +
        '          document.body.appendChild(canvas);\n' +
        '      }\n' +
        '      return canvas.getContext(\'2d\');\n' +
        '    }\n' +
        '  }\n' +
        '}\n' +
        '\n' +
        'var game = new Conway.GameOfLife();\n')
  }, [])

  return (
      <>
        <div className="gf-editor-base gf-controls">
        <Controls/>
        </div>
        <div className="gf-editor-base gf-editor">
          <CodeMirror
              theme={myTheme}
              extensions={[javascript({ typescript: true })]}
              value={value}
              initialState={
                serializedState
                    ? {
                      json: JSON.parse(serializedState || ''),
                      fields: stateFields,
                    }
                    : undefined
              }
              onChange={(value, viewUpdate) => {
                localStorage.setItem('codeValue', value);

                const state = viewUpdate.state.toJSON(stateFields);
                localStorage.setItem('editorState', JSON.stringify(state));
              }}
          />
        </div>
        <div className="gf-editor-base gf-editor">
          <CodeMirror
              theme={myTheme}
              extensions={[javascript({ typescript: true })]}
              value={"// Write your test request here"}
              initialState={
                serializedState
                    ? {
                      json: JSON.parse(serializedState || ''),
                      fields: stateFields,
                    }
                    : undefined
              }
              onChange={(value, viewUpdate) => {
                localStorage.setItem('codeValue', value);

                const state = viewUpdate.state.toJSON(stateFields);
                localStorage.setItem('editorState', JSON.stringify(state));
              }}
          />
        </div>
        <div className="gf-editor-base gf-error">
          <div>
            <span
                className="bg-red-100 text-red-800 text-md font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-red-400 border border-red-400">Error</span>
          </div>
            <span>
              Uncaught Error: There was an error while hydrating. Because the error happened outside of a Suspense boundary, the entire root will switch to client rendering.
    at updateHostRoot (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:15468:57)
    at beginWork$1 (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:17338:14)
    at beginWork (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:25689:14)
    at performUnitOfWork (webpack-internal:///(app-client)/./node_modules/next/dist/compiled/react-dom/cjs/react-dom.development.js:24540:12)
            </span>
        </div>
        <div className="gf-editor-base gf-stdout">
          <div>
            <span
                className="bg-yellow-100 text-yellow-800 text-md font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-yellow-400 border border-yellow-300">Console</span>
          </div>
          <span>
            - ready started server on 0.0.0.0:3000, url: http://localhost:3000
warning ../package.json: No license field
- event compiled client and server successfully in 377 ms (20 modules)
- wait compiling...
- event compiled client and server successfully in 118 ms (20 modules)
- wait compiling /code/page (client and server)...
- event compiled client and server successfully in 4.1s (3295 modules)
- warn
          </span>
        </div>
        <div className="gf-editor-base gf-result">
          <div>
            <span
                className="bg-green-100 text-green-800 text-xs font-medium mr-2 px-2.5 py-0.5 rounded dark:bg-gray-900 dark:text-green-400 border border-green-400">Result</span>
          </div>
          <span>
            {"{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"temperature\": 0.7,\n" +
                "  \"user\": \"u_N6jrfCkk\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"PROMPT: What is the current time and weather in Boston?\\n\\nANSWER:\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"function\",\n" +
                "      \"name\": \"get_city_time_and_weather\",\n" +
                "      \"content\": {\n" +
                "        \"location\": \"Boston, MA\",\n" +
                "        \"temperature\": \"35\",\n" +
                "        \"forecast\": [\n" +
                "          \"rainy\",\n" +
                "          \"windy\"\n" +
                "        ],\n" +
                "        \"current_time\": \"August 1st 2023, 9:58:02 pm\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"max_tokens\": 1000\n" +
                "}"}
          </span>
        </div>
        </>
  );
}
export default CodeEditor;