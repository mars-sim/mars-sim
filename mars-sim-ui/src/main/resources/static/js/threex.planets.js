var THREEx = THREEx || {}

THREEx.Planets	= {}

THREEx.Planets.baseURL	= 'img/'

var loader = new THREE.TextureLoader();

// from http://planetpixelemporium.com/

THREEx.Planets.createHabital	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64);
	var texture	= loader.load(THREEx.Planets.baseURL+'planet_1.jpg');
	texture.minFilter = THREE.LinearFilter;

	var material	= new THREE.MeshPhongMaterial({
		map	: texture
	});

	var mesh	= new THREE.Mesh(geometry, material)
	//mesh.material.needsUpdate = true;
	return mesh
}

THREEx.Planets.createSun	= function(size){
	//var geometry	= new THREE.SphereGeometry(0.5, 64, 64)
	var geometry	= new THREE.SphereGeometry(size, 64, 64);
	var texture	= loader.load(THREEx.Planets.baseURL+'sunmap2.jpg')

	var material	= new THREE.MeshLambertMaterial({
		map	: texture,
		transparent: true,
		opacity: 1
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createMercury	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var material	= new THREE.MeshPhongMaterial({
		map	: loader.load(THREEx.Planets.baseURL+'mercurymap.jpg'),
		bumpMap	: loader.load(THREEx.Planets.baseURL+'mercurybump.jpg'),
		bumpScale: 0.005,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createVenus	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var material	= new THREE.MeshPhongMaterial({
		map	: loader.load(THREEx.Planets.baseURL+'venusmap.jpg'),
		bumpMap	: loader.load(THREEx.Planets.baseURL+'venusbump.jpg'),
		bumpScale: 0.005,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createEarth	= function(){
	var geometry	= new THREE.SphereGeometry(0.5, 32, 32)
	var material	= new THREE.MeshPhongMaterial({
		map		: THREE.ImageUtils.loadTexture(THREEx.Planets.baseURL+'earthmap1k.jpg'),
		bumpMap		: THREE.ImageUtils.loadTexture(THREEx.Planets.baseURL+'earthbump1k.jpg'),
		bumpScale	: 0.05,
		specularMap	: THREE.ImageUtils.loadTexture(THREEx.Planets.baseURL+'earthspec1k.jpg'),
		specular	: new THREE.Color('grey'),
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh	
}


THREEx.Planets.createMoon	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)

	var textureMap = loader.load(THREEx.Planets.baseURL+'moonmap1k.jpg');
	textureMap.minFilter = THREE.LinearFilter;

	var bumpMap = loader.load(THREEx.Planets.baseURL+'moonbump1k.jpg');
	bumpMap.minFilter = THREE.LinearFilter;

	var material	= new THREE.MeshPhongMaterial({
		map	: textureMap,
		bumpMap	: bumpMap,
		bumpScale: 0.002,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createMars	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var material	= new THREE.MeshPhongMaterial({
		map	: loader.load(THREEx.Planets.baseURL+'marsmap1k.jpg'),
		bumpMap	: loader.load(THREEx.Planets.baseURL+'marsbump1k.jpg'),
		bumpScale: 0.05,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}
/*
THREEx.Planets.createPhobos	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)

	var textureMap = loader.load(THREEx.Planets.baseURL+'phobos1k.jpg');
	textureMap.minFilter = THREE.LinearFilter;

	var bumpMap = loader.load(THREEx.Planets.baseURL+'phobosbump1k.jpg');
	bumpMap.minFilter = THREE.LinearFilter;

	var material	= new THREE.MeshPhongMaterial({
		map	: textureMap,
		bumpMap	: bumpMap,
		bumpScale: 0.002,
	})
	
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}
*/

THREEx.Planets.createJupiter	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var texture	= loader.load(THREEx.Planets.baseURL+'jupitermap.jpg')
	var material	= new THREE.MeshPhongMaterial({
		map	: texture,
		bumpMap	: texture,
		bumpScale: 0.02,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}


THREEx.Planets.createSaturn	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var texture	= loader.load(THREEx.Planets.baseURL+'saturnmap.jpg')
	var material	= new THREE.MeshPhongMaterial({
		map	: texture,
		bumpMap	: texture,
		bumpScale: 0.05,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}


THREEx.Planets.createSaturnRing	= function(size){
	// create destination canvas
	var canvasResult	= document.createElement('canvas')
	canvasResult.width	= 915
	canvasResult.height	= 64
	var contextResult	= canvasResult.getContext('2d')

	// load earthcloudmap
	var imageMap	= new Image();
	imageMap.addEventListener("load", function() {

		// create dataMap ImageData for earthcloudmap
		var canvasMap	= document.createElement('canvas')
		canvasMap.width	= imageMap.width
		canvasMap.height= imageMap.height
		var contextMap	= canvasMap.getContext('2d')
		contextMap.drawImage(imageMap, 0, 0)
		var dataMap	= contextMap.getImageData(0, 0, canvasMap.width, canvasMap.height)

		// load earthcloudmaptrans
		var imageTrans	= new Image();
		imageTrans.addEventListener("load", function(){
			// create dataTrans ImageData for earthcloudmaptrans
			var canvasTrans		= document.createElement('canvas')
			canvasTrans.width	= imageTrans.width
			canvasTrans.height	= imageTrans.height
			var contextTrans	= canvasTrans.getContext('2d')
			contextTrans.drawImage(imageTrans, 0, 0)
			var dataTrans		= contextTrans.getImageData(0, 0, canvasTrans.width, canvasTrans.height)
			// merge dataMap + dataTrans into dataResult
			var dataResult		= contextMap.createImageData(canvasResult.width, canvasResult.height)
			for(var y = 0, offset = 0; y < imageMap.height; y++){
				for(var x = 0; x < imageMap.width; x++, offset += 4){
					dataResult.data[offset+0]	= dataMap.data[offset+0]
					dataResult.data[offset+1]	= dataMap.data[offset+1]
					dataResult.data[offset+2]	= dataMap.data[offset+2]
					dataResult.data[offset+3]	= 255 - dataTrans.data[offset+0]/4
				}
			}
			// update texture with result
			contextResult.putImageData(dataResult,0,0)
			material.map.needsUpdate = true;
		})
		imageTrans.src	= THREEx.Planets.baseURL+'saturnringpattern.gif';
	}, false);
	imageMap.src	= THREEx.Planets.baseURL+'saturnringcolor.jpg';

	var geometry	= new THREEx.Planets._RingGeometry(size*2, size*3, 64);
	var material	= new THREE.MeshPhongMaterial({
		map		: new THREE.Texture(canvasResult),
		// map		: loader.load(THREEx.Planets.baseURL+'ash_uvgrid01.jpg'),
		side		: THREE.DoubleSide,
		transparent	: true,
		opacity		: 0.8,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	mesh.lookAt(new THREE.Vector3(0.5,-4,1))
	return mesh
}


THREEx.Planets.createUranus	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var texture	= loader.load(THREEx.Planets.baseURL+'uranusmap.jpg')
	var material	= new THREE.MeshPhongMaterial({
		map	: texture,
		bumpMap	: texture,
		bumpScale: 0.05,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createUranusRing	= function(size){
	// create destination canvas
	var canvasResult	= document.createElement('canvas')
	canvasResult.width	= 1024
	canvasResult.height	= 72
	var contextResult	= canvasResult.getContext('2d')

	// load earthcloudmap
	var imageMap	= new Image();
	imageMap.addEventListener("load", function() {

		// create dataMap ImageData for earthcloudmap
		var canvasMap	= document.createElement('canvas')
		canvasMap.width	= imageMap.width
		canvasMap.height= imageMap.height
		var contextMap	= canvasMap.getContext('2d')
		contextMap.drawImage(imageMap, 0, 0)
		var dataMap	= contextMap.getImageData(0, 0, canvasMap.width, canvasMap.height)

		// load earthcloudmaptrans
		var imageTrans	= new Image();
		imageTrans.addEventListener("load", function(){
			// create dataTrans ImageData for earthcloudmaptrans
			var canvasTrans		= document.createElement('canvas')
			canvasTrans.width	= imageTrans.width
			canvasTrans.height	= imageTrans.height
			var contextTrans	= canvasTrans.getContext('2d')
			contextTrans.drawImage(imageTrans, 0, 0)
			var dataTrans		= contextTrans.getImageData(0, 0, canvasTrans.width, canvasTrans.height)
			// merge dataMap + dataTrans into dataResult
			var dataResult		= contextMap.createImageData(canvasResult.width, canvasResult.height)
			for(var y = 0, offset = 0; y < imageMap.height; y++){
				for(var x = 0; x < imageMap.width; x++, offset += 4){
					dataResult.data[offset+0]	= dataMap.data[offset+0]
					dataResult.data[offset+1]	= dataMap.data[offset+1]
					dataResult.data[offset+2]	= dataMap.data[offset+2]
					dataResult.data[offset+3]	= 255 - dataTrans.data[offset+0]/2
				}
			}
			// update texture with result
			contextResult.putImageData(dataResult,0,0)
			material.map.needsUpdate = true;
		})
		imageTrans.src	= THREEx.Planets.baseURL+'uranusringtrans.gif';
	}, false);
	imageMap.src	= THREEx.Planets.baseURL+'uranusringcolour.jpg';

	var geometry	= new THREEx.Planets._RingGeometry(size*2, size*2.5, 64);
	var material	= new THREE.MeshPhongMaterial({
		map		: new THREE.Texture(canvasResult),
		// map		: loader.load(THREEx.Planets.baseURL+'ash_uvgrid01.jpg'),
		side		: THREE.DoubleSide,
		transparent	: true,
		opacity		: 0.8,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	mesh.lookAt(new THREE.Vector3(0.5,-4,1))
	return mesh
}


THREEx.Planets.createNeptune	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var texture	= loader.load(THREEx.Planets.baseURL+'neptunemap.jpg')
	var material	= new THREE.MeshPhongMaterial({
		map	: texture,
		bumpMap	: texture,
		bumpScale: 0.05,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createPluto	= function(size){
	var geometry	= new THREE.SphereGeometry(size, 64, 64)
	var material	= new THREE.MeshPhongMaterial({
		map	: loader.load(THREEx.Planets.baseURL+'plutomap1k.jpg'),
		bumpMap	: loader.load(THREEx.Planets.baseURL+'plutobump1k.jpg'),
		bumpScale: 0.005,
	})
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}

THREEx.Planets.createStarfield	= function(){
	var texture	= loader.load(THREEx.Planets.baseURL+'galaxy_starfield.png')
	var material	= new THREE.MeshBasicMaterial({
		map	: texture,
		side	: THREE.BackSide
	})
	var geometry	= new THREE.SphereGeometry(100, 64, 64)
	var mesh	= new THREE.Mesh(geometry, material)
	return mesh
}


//////////////////////////////////////////////////////////////////////////////////
//		comment								//
//////////////////////////////////////////////////////////////////////////////////

/**
 * change the original from three.js because i needed different UV
 *
 * @author Kaleb Murphy
 * @author jerome etienne
 */
THREEx.Planets._RingGeometry = function ( innerRadius, outerRadius, thetaSegments ) {

	THREE.Geometry.call( this )

	innerRadius	= innerRadius || 0
	outerRadius	= outerRadius || 100
	thetaSegments	= thetaSegments	|| 8

	var normal	= new THREE.Vector3( 0, 0, 1 )

	for(var i = 0; i < thetaSegments; i++ ){
		var angleLo	= (i / thetaSegments) *Math.PI*2
		var angleHi	= ((i+1) / thetaSegments) *Math.PI*2

		var vertex1	= new THREE.Vector3(innerRadius * Math.cos(angleLo), innerRadius * Math.sin(angleLo), 0);
		var vertex2	= new THREE.Vector3(outerRadius * Math.cos(angleLo), outerRadius * Math.sin(angleLo), 0);
		var vertex3	= new THREE.Vector3(innerRadius * Math.cos(angleHi), innerRadius * Math.sin(angleHi), 0);
		var vertex4	= new THREE.Vector3(outerRadius * Math.cos(angleHi), outerRadius * Math.sin(angleHi), 0);

		this.vertices.push( vertex1 );
		this.vertices.push( vertex2 );
		this.vertices.push( vertex3 );
		this.vertices.push( vertex4 );


		var vertexIdx	= i * 4;

		// Create the first triangle
		var face = new THREE.Face3(vertexIdx + 0, vertexIdx + 1, vertexIdx + 2, normal);
		var uvs = []

		var uv = new THREE.Vector2(0, 0)
		uvs.push(uv)
		var uv = new THREE.Vector2(1, 0)
		uvs.push(uv)
		var uv = new THREE.Vector2(0, 1)
		uvs.push(uv)

		this.faces.push(face);
		this.faceVertexUvs[0].push(uvs);

		// Create the second triangle
		var face = new THREE.Face3(vertexIdx + 2, vertexIdx + 1, vertexIdx + 3, normal);
		var uvs = []

		var uv = new THREE.Vector2(0, 1)
		uvs.push(uv)
		var uv = new THREE.Vector2(1, 0)
		uvs.push(uv)
		var uv = new THREE.Vector2(1, 1)
		uvs.push(uv)

		this.faces.push(face);
		this.faceVertexUvs[0].push(uvs);
	}

	//this.computeCentroids();
	this.computeFaceNormals();

	this.boundingSphere = new THREE.Sphere( new THREE.Vector3(), outerRadius );

};
THREEx.Planets._RingGeometry.prototype = Object.create( THREE.Geometry.prototype );
