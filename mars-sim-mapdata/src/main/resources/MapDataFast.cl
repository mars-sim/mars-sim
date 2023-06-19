constant float TWO_1_PI = (0.5 * M_1_PI_F);

float2 rectToSphere(int2 pos, float2 coord, float rho, float TWO_PI) {
		 float sinPhi = half_sin(coord.x);
		 float sinTheta = half_sin(coord.y);
		 float cosPhi = half_cos(coord.x);
		 float cosTheta = half_cos(coord.y);

		 int2 pos2 = pos * pos;

		 float z = half_sqrt((rho * rho) - pos2.x - pos2.y);

		 float y2 = (pos.y * cosPhi) + (z * sinPhi);
		 float z2 = (z * cosPhi) - (pos.y * sinPhi);

		 float x3 = (pos.x * cosTheta) + (y2 * sinTheta);
		 float y3 = (y2 * cosTheta) - (pos.x * sinTheta);
		 float z3 = z2;

		 float phiNew = acos(z3 / rho);
		 float thetaNew = asin(x3 / (rho * half_sin(phiNew)));

		 if (x3 >= 0) {
			 if (y3 < 0)
				 thetaNew = M_PI - thetaNew;
		 } else {
			 if (y3 < 0)
				 thetaNew = M_PI - thetaNew;
			 else
				 thetaNew = TWO_PI + thetaNew;
		 }

		 return (float2)(phiNew, thetaNew);
}

int2 getRGBColorInt(float phi, float theta, int pixelWidth, int pixelHeight, double TWO_PI) {
    // Make sure phi is between 0 and PI.

    float2 aa = (float2)(fabs(phi - M_PI_F), pixelHeight) * M_1_PI_F;
    float2 bb = (float2)(fabs(theta), pixelWidth) * TWO_1_PI;

    float t = M_PI_F * ceil(aa.x);
    phi -= (phi < 0 - phi > M_PI) * t;

    // Adjust theta with PI for the map offset.
    // Note: the center of the map is when theta = 0
    bool val = (theta > M_PI_F);
    theta += copysign(M_PI_F, (!val - val));

    // Make sure theta is between 0 and 2 PI.
    float v = TWO_PI * ceil(bb.x);
    theta += (theta < 0 - theta > TWO_PI) * v;

    int row = trunc(phi * (aa.y));
    row = min(row, pixelHeight - 1);

    int column = trunc(theta * (bb.y));
    column = min(column, pixelWidth - 1);

    return (int2)(column,row);
}

kernel
void getMapImage(
double centerPhi,
double centerTheta,
int mapBoxWidth,
int mapBoxHeight,
int pixelWidth,
int pixelHeight,
int halfWidth,
int halfHeight,
int numElements,
global int* colOut,
global int* rowOut,
double TWO_PI,
double rho) {
  int iGID = get_global_id(0);

  if(iGID > numElements) {
    return;
  }

  // Faster method may be to remove % mapBoxWidth and replace with a 2D kernel and a for loop that iterates over local group??
  int2 pos = (int2)(iGID % mapBoxWidth, half_divide(iGID, mapBoxWidth)) - (int2)(halfWidth,halfHeight);

  float2 sph = rectToSphere(pos, (float2)(centerPhi, centerTheta), (float)rho, (float)TWO_PI);

  int2 loc = getRGBColorInt(sph.x, sph.y, pixelWidth, pixelHeight, TWO_PI);

  colOut[iGID] = loc.x;
  rowOut[iGID] = loc.y;
}
