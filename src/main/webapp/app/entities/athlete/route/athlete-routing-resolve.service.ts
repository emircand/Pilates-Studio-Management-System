import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAthlete } from '../athlete.model';
import { AthleteService } from '../service/athlete.service';

export const athleteResolve = (route: ActivatedRouteSnapshot): Observable<null | IAthlete> => {
  const id = route.params['id'];
  if (id) {
    return inject(AthleteService)
      .find(id)
      .pipe(
        mergeMap((athlete: HttpResponse<IAthlete>) => {
          if (athlete.body) {
            return of(athlete.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default athleteResolve;
