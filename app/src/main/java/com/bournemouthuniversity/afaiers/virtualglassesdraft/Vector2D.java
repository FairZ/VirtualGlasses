package com.bournemouthuniversity.afaiers.virtualglassesdraft;

/*
    2 dimensional mathematical vector class used in transformation calculations as well as some other things
*/
public class Vector2D {
    public float x;
    public float y;

   Vector2D()
   {
       x = 0;
       y = 0;
   }

   Vector2D(float _x, float _y)
   {
       x= _x;
       y= _y;
   }

   public static float DotProduct(Vector2D _a, Vector2D _b){
       return (_a.x*_b.x) + (_a.y*_b.y);
   }

   public static float CompOfBOnA(Vector2D _a, Vector2D _b){
       //found out the length of the component of B which is parallel to A
       return DotProduct(_a,_b)/_a.Magnitude();
   }

   public float Magnitude(){
       return (float) Math.sqrt(x*x + y*y);
   }

   public Vector2D Normalise()
   {
       float length = Magnitude();
       return new Vector2D(x / length, y / length);
   }

   public void Add(Vector2D _b){
       this.x += _b.x;
       this.y += _b.y;
   }

    public void Subtract(Vector2D _b){
        this.x -= _b.x;
        this.y -= _b.y;
    }

    public static float GetAngleBetween(Vector2D _a, Vector2D _b)
    {
        //calculate the angle between A and B
        //will always be <180 so further direction calculations must be done by the user
        return (float) Math.toDegrees( Math.acos(DotProduct(_a,_b)) );
    }

    public void RotateAntiClockwise(float _degrees){
        double radians = Math.toRadians(_degrees);
        float x1 = (float) ((x*Math.cos(radians)) - (y* Math.sin(radians)));
        float y1 = (float) ((y*Math.cos(radians))+(x*Math.sin(radians)));
        x = x1;
        y = y1;
    }
}
