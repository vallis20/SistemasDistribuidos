/*
 * TEMPLATE
 */

/*Se declara la estructura del xdr que nos ayudará a convertir los dos enteros (a y b) 
a un formato estándar*/

#include "calculadora.h"

bool_t
xdr_dupla_int (XDR *xdrs, dupla_int *objp)
{
	register int32_t *buf;

	 if (!xdr_int (xdrs, &objp->a))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->b))
		 return FALSE;
	return TRUE;
}
