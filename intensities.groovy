guiscript=true
// Script to measure intensity in cells, cytoplasm, and membrane
// This script deletes all child annotations. It then marks all annotations as "cell". 
// Then it creates for each cell a child annotation of "membrane" and "cytoplasm".


resetSelection();

// Add classes cytoplasm , membrane , cell
def classes = getQuPath().getAvailablePathClasses().findAll {it -> it}  // copy list to avoid triggering JavaFX
if (!(getPathClass("cytoplasm") in classes)) {
    classes.add(getPathClass("cytoplasm"))
}    
if (!(getPathClass("membrane") in classes)) {
    classes.add(getPathClass("membrane"))
}    
if (!(getPathClass("cell") in classes)) {
    classes.add(getPathClass("cell"))
}    
getQuPath().getAvailablePathClasses().setAll(classes)
print(getQuPath().getAvailablePathClasses())


// Remove child annotations and keep only root annotations
top_annotations = getCurrentHierarchy().getRootObject().getChildObjects().findAll {it.isAnnotation()}
child_annotations = getAnnotationObjects().findAll{ (! (it in top_annotations)) }
print("Removing: " + child_annotations )
removeObjects(child_annotations , false)


// Mark top annotations as "cell"
top_annotations.forEach { p -> p.setPathClass(getPathClass("cell"));  print(p)}


// Create "cytoplasm" annotations
selectObjects(top_annotations)
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusPixels":-25.0,"lineCap":"ROUND","removeInterior":false,"constrainToParent":true}')
top_annotations.forEach { top ->
    top.getChildObjects().forEach {  child ->
        if (child.getPathClass() == getPathClass("cell")) { 
            child.setPathClass(getPathClass("cytoplasm"))
            child.setLocked(true);
            fireHierarchyUpdate()
            }
    } 
}

// Create "membrane" annotations
selectObjects(top_annotations)
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusPixels":-25.0,"lineCap":"ROUND","removeInterior":true,"constrainToParent":true}')
top_annotations.forEach { top ->
    top.getChildObjects().forEach {  child ->
        if (child.getPathClass() == getPathClass("cell")) { 
            child.setPathClass(getPathClass("membrane"))
            child.setLocked(true);
            fireHierarchyUpdate()
            }
    } 
}

// Intensity Features
runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin', '{"downsample":1.0,"region":"ROI","tileSizePixels":200.0,"colorRed":true,"colorGreen":true,"colorBlue":true,"colorHue":false,"colorSaturation":false,"colorBrightness":false,"doMean":true,"doStdDev":true,"doMinMax":false,"doMedian":false,"doHaralick":false,"haralickDistance":1,"haralickBins":32}')

fireHierarchyUpdate()