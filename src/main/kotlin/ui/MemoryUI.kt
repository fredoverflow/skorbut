package ui

import common.Counter
import freditor.Fronts
import interpreter.Memory
import interpreter.PointerValue
import interpreter.Segment
import semantic.types.ArrayType
import semantic.types.StructType
import semantic.types.Type

import java.awt.*
import java.awt.geom.Line2D
import java.awt.geom.QuadCurve2D

import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder
import kotlin.math.pow
import kotlin.math.sqrt

class MemoryUI(var memory: Memory) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    var active = true

    private val emptyBorder = EmptyBorder(8, 8, 8, 8)
    private val stringsBorder = LineBorder(Color.LIGHT_GRAY, 2, true)
    private val staticsBorder = LineBorder(Color.GRAY, 2, true)
    private val stackBorder = LineBorder(Color.BLUE, 2, true)
    private val heapBorder = LineBorder(Color(128, 0, 128), 2, true)
    private var lineBorder = stringsBorder
    private val rigidWidth = Dimension(300, 0)

    private fun Segment.objectComponent(title: String, qualified: Type): JComponent {
        val type = qualified.unqualified()
        val address = valueIndex
        val component = when (type) {
            is ArrayType -> arrayComponent(type)
            is StructType -> structComponent(type)
            else -> scalarComponent()
        }
        component.alignmentX = Component.LEFT_ALIGNMENT
        component.border = CompoundBorder(emptyBorder, TitledBorder(lineBorder, title, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Fronts.sansSerif))
        objects[this]!![address][type] = component
        return component
    }

    private fun Segment.arrayComponent(type: ArrayType): JComponent {
        val cells = JPanel()
        val axis = if (type.dimensions().and(1) == 1) BoxLayout.X_AXIS else BoxLayout.Y_AXIS
        cells.layout = BoxLayout(cells, axis)
        for (i in 0 until type.size) {
            cells.add(objectComponent(i.toString(), type.elementType))
        }
        return cells
    }

    private fun Segment.structComponent(type: StructType): JComponent {
        val cells = JPanel()
        val axis = BoxLayout.Y_AXIS
        cells.layout = BoxLayout(cells, axis)
        for (symbol in type.members) {
            val name = symbol.name.text
            if (!name.startsWith('_')) {
                cells.add(objectComponent(name, symbol.type))
            } else {
                valueIndex += symbol.type.count()
            }
        }
        return cells
    }

    private fun Segment.scalarComponent(): JComponent {
        val value = this[valueIndex++]
        val scalar = JLabel(" %3s ".format(value.show()))
        scalar.font = Fronts.monospaced
        if (value is PointerValue && value.referenced.isReferable()) {
            pointers[scalar] = value
        }
        return scalar
    }

    private val objects = HashMap<Segment, MutableList<HashMap<Type, JComponent>>>()
    private val pointers = LinkedHashMap<JComponent, PointerValue>()
    private var valueIndex = 0

    fun update() {
        if (active) {
            removeAll()
            objects.clear()
            pointers.clear()
            updateSingleSegment(memory.stringConstants, stringsBorder)
            updateSingleSegment(memory.staticVariables, staticsBorder)
            updateMultipleSegments(memory.heap, heapBorder)
            add(Box.createVerticalGlue())
            updateMultipleSegments(memory.stack, stackBorder)
            revalidate()
            repaint()
        }
    }

    private fun updateSingleSegment(segment: Segment, border: LineBorder) {
        val structType = segment.type as StructType
        if (structType.members.isNotEmpty()) {
            lineBorder = border
            update(segment)
        }
    }

    private fun update(segment: Segment) {
        objects[segment] = generateSequence { HashMap<Type, JComponent>() }.take(segment.type.count()).toMutableList()
        valueIndex = 0
        with(segment) {
            if (type is StructType) {
                val title = "%04x  %s".format(address.and(0xffff), type.name.text)
                val component = objectComponent(title, type)
                val rigidArea = Box.createRigidArea(rigidWidth) as JComponent
                rigidArea.alignmentX = JComponent.LEFT_ALIGNMENT
                component.add(rigidArea)
                add(component)
            } else {
                val title = "%04x".format(address.and(0xffff))
                add(objectComponent(title, type))
            }
        }
    }

    private fun updateMultipleSegments(segments: List<Segment>, border: LineBorder) {
        lineBorder = border
        segments.asReversed().forEach(::update)
    }

    override fun paint(graphics: Graphics) {
        super.paint(graphics)
        val g2d = graphics as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        g2d.stroke = stroke
        g2d.color = color
        val pointerCounter = Counter()
        for ((source, pointer) in pointers.entries) {
            val place = pointerCounter.count(pointer)
            val obj = pointer.referenced
            val offset = if (obj.isSentinel()) obj.minus(1).offset else obj.offset
            val target = objects[obj.segment]!![offset][obj.type.unqualified()]!!
            val sourcePos = SwingUtilities.convertPoint(source, 0, 0, this)
            val targetPos = SwingUtilities.convertPoint(target, 0, 0, this)
            val x1 = sourcePos.x + source.width / 2
            val y1 = sourcePos.y + source.height / 2
            val x2 = targetPos.x + if (obj.isSentinel()) target.width else lerp(source.width, 0.5.pow(place * 0.5), 16)
            var y2 = targetPos.y + 8
            if (y2 < y1) {
                y2 = targetPos.y + target.height - 8
            }
            drawPointer(g2d, x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
        }
    }

    private fun lerp(zero: Int, x: Double, one: Int): Int = (zero.toDouble() * (1 - x) + one.toDouble() * x).toInt()

    private fun drawPointer(g2d: Graphics2D, x1: Double, y1: Double, x2: Double, y2: Double) {
        var deltaX = x2 - x1
        var deltaY = y2 - y1
        if (deltaY > 0) {
            deltaX = -deltaX
            deltaY = -deltaY
        }
        val controlX = (x1 + x2 - deltaY) * 0.5
        val controlY = (y1 + y2 + deltaX) * 0.5
        g2d.draw(QuadCurve2D.Double(x1, y1, controlX, controlY, x2, y2))

        deltaX = controlX - x2
        deltaY = controlY - y2
        val scaleFactor = 8.0 / sqrt((deltaX * deltaX + deltaY * deltaY) * 2)
        deltaX *= scaleFactor
        deltaY *= scaleFactor
        g2d.draw(Line2D.Double(x2, y2, x2 + (deltaX - deltaY), y2 + (deltaY + deltaX)))
        g2d.draw(Line2D.Double(x2, y2, x2 + (deltaX + deltaY), y2 + (deltaY - deltaX)))
    }

    private val stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val color = Color(1.0f, 0.5f, 0f, 0.75f)
}
